package com.fleet.knock.wallpaper

import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.fleet.knock.R
import com.fleet.knock.info.repository.WallpaperRepository
import com.fleet.knock.info.user.UserConfig
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.requester.Requester
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import java.util.*

class ExoPlayerWallpaper : WallpaperService() {

    override fun onCreateEngine() = VideoEngine()

    inner class VideoEngine : Engine() {
        private val crashlytics = FirebaseCrashlytics.getInstance()

        private val repository = WallpaperRepository.get(application)

        private val config = repository.getUserConfig()

        private val defaultResource = RawResourceDataSource(application).let { dataSource ->
            dataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.mp4_default_splash)))
            DataSource.Factory { dataSource }.let { factory ->
                ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
            }
        }

        private val logApplyUserRequester = LogApplyUserRequester()
        private val logApplyThemeRequester = LogApplyThemeRequester()
        private val refreshRequester = RefreshRequester()

        private var currentSource: ProgressiveMediaSource? = null

        private suspend fun getResource(cfg: UserConfig): ProgressiveMediaSource? = withContext(
            Dispatchers.IO
        ) {
            when{
                cfg.isValidPublicTheme -> {
                    Log.d("EXO_WALLPAPER", "Surface getResource is public")
                    repository.getTheme(cfg.selectedPublicThemeId)?.getThemeSource(application)
                }
                cfg.isValidLocalTheme -> {
                    Log.d("EXO_WALLPAPER", "Surface getResource is local")
                    repository.getTheme(cfg.selectedLocalThemeId)?.getThemeSource(application)
                }
                else -> {
                    Log.d("EXO_WALLPAPER", "Surface getResource is null")
                    null
                }
            }
        }

        private fun loadResource(cfg: UserConfig?) {
            cfg ?: return
            refreshRequester.refresh()

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    var count = 0
                    do {
                        currentSource = getResource(cfg)
                        count++
                    } while (count < 5 && currentSource == null);

                    Log.d("EXO_WALLPAPER", "Surface loadResource count : $count")

                    player?.apply {
                        playWhenReady = false
                        stop()
                        prepare(currentSource ?: defaultResource)
                        playWhenReady = true
                    }
                }
                catch(e:Exception){
                    crashlytics.log("wallpaper load error : ${e.message}")
                }
            }
        }

        private var player: SimpleExoPlayer? = null

        private val observer = Observer<UserConfig?> { config ->
            loadResource(config)
        }


        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)

            Log.d("EXO_WALLPAPER", "Engine Surface Create")

            player = SimpleExoPlayer.Builder(
                application,
                DefaultRenderersFactory(applicationContext).apply {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                }).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0.0f
                setVideoSurface(holder?.surface)
            }

            config.observeForever(observer)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            config.removeObserver(observer)

            player?.stop()
            player?.release()
            player = null

            Log.d("EXO_WALLPAPER", "Engine Surface Destroyed")

            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            player?.playWhenReady = visible

            if (visible) {
                requestLog()
                requestRefresh()
            }

            Log.d("EXO_WALLPAPER", "Surface visibility : $visible")
        }

        private fun requestLog() {
            logApplyThemeRequester.request()
            logApplyUserRequester.request()
        }

        private fun requestRefresh(){
            val cfg = config.value ?: return
            if(refreshRequester.request()){
                loadResource(cfg)
            }
        }
    }

    class RefreshRequester : Requester() {
        private var nextRefreshTime = 0L

        override val isRequest: Boolean
            get() = nextRefreshTime <= System.currentTimeMillis()

        init {
            refresh()
        }

        override fun refresh() {
            nextRefreshTime = System.currentTimeMillis() + 3600000L
        }

        override fun doWork() {
        }
    }

    inner class LogApplyUserRequester : Requester() {
        private val config = PreferenceManager.getDefaultSharedPreferences(application)

        private var requestUsingThemeLogTime =
            config.getLong(CONFIG_REQUEST_USING_THEME_LOG_TIME, 0L)

        override val isRequest
            get() = requestUsingThemeLogTime <= System.currentTimeMillis()

        override fun refresh() {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DATE, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            config.edit().apply {
                putLong(CONFIG_REQUEST_USING_THEME_LOG_TIME, cal.timeInMillis)
                apply()
            }

            requestUsingThemeLogTime = cal.timeInMillis
        }

        override fun doWork() {
            GAUtil.get(application).logApplyTheme()
        }
    }

    inner class LogApplyThemeRequester : Requester() {
        private val repository = WallpaperRepository.get(application)

        override val isRequest: Boolean
            get() = true

        override fun refresh() {}

        override fun doWork() {
            GlobalScope.launch(Dispatchers.IO) {
                val uConfig = repository.getUserConfigSync() ?: return@launch

                when{
                    uConfig.isValidPublicTheme ->{
                        val t = repository.getTheme(uConfig.selectedPublicThemeId) ?: return@launch
                        GAUtil.get(application).logUsingFreeTheme(t.id, t.themeTitle)
                    }
                    uConfig.isValidLocalTheme -> {
                        GAUtil.get(application).logUsingLocalTheme()
                    }
                    else ->{
                        GAUtil.get(application).logUsingDefaultTheme()
                    }
                }
            }
        }
    }

    companion object {
        private const val CONFIG_REQUEST_USING_THEME_LOG_TIME =
            "Config.RequestUsingThemeLogTime"
    }
}