package com.fleet.knock.ui.page.splash

import android.app.Application
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.info.theme.FThemeEntity
import com.fleet.knock.utils.dataloader.*
import com.fleet.knock.utils.viewmodel.BaseViewModel
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : BaseViewModel(application) {
    private val repository = FThemeRepository.get(application)

    private var isTimeOut = MutableLiveData(false)
    private var isLoadComplete = MutableLiveData(false)
    val isComplete = MediatorLiveData<Boolean>().apply{
        addSource(isTimeOut){
            isComplete(it, isLoadComplete.value)
        }
        addSource(isLoadComplete){
            isComplete(isTimeOut.value, it)
        }
    }
    private fun isComplete(isTimeOut:Boolean?, isLoadComplete:Boolean?){
        isTimeOut?: return
        isLoadComplete ?: return

        isComplete.value = isTimeOut && isLoadComplete
    }

    private val recommend = MutableLiveData<FTheme>()
    val recommendSource = Transformations.map(recommend){
        it?.getThemeSource(application) ?: defaultSource
    }
    private val defaultSource = RawResourceDataSource(getApplication()).let{ dataSource ->
        dataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.mp4_default_splash)))
        DataSource.Factory { dataSource }.let{ factory ->
            ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
        }
    }
    val recommendPreload = Transformations.map(recommend){
        it?.getPreload(application)
    }
    val recommendVisibility = Transformations.map(recommend){
        if(it == null) View.INVISIBLE
        else View.VISIBLE
    }

    val recommendCopyright = Transformations.map(recommend){
        it?.themeCopyright ?: ""
    }

    init{
        initConfig()

        loadRecommend()

        waitTime()

        initResource()
    }

    private fun initConfig(){
        if(!config.getBoolean(CONFIG_RESET_UPDATE_TIME_V27, false)){
            val configKeyList = listOf(
                FDLUser.CONFIG_RECENT_UPDATE_TIME,
                FDLUserDelete.CONFIG_RECENT_UPDATE_TIME,
                FDLTheme.CONFIG_RECENT_UPDATE_TIME,
                FDLThemeDelete.CONFIG_RECENT_UPDATE_TIME,
                FDLRecommend.CONFIG_RECENT_UPDATE_TIME,
                FDLRecommendDelete.CONFIG_RECENT_UPDATE_TIME,
                FDLPromotion.CONFIG_RECENT_UPDATE_TIME,
                FDLPromotionDelete.CONFIG_RECENT_UPDATE_TIME,
                FDLProject.CONFIG_RECENT_UPDATE_TIME,
                FDLProjectDelete.CONFIG_RECENT_UPDATE_TIME,
                FDLFrame.CONFIG_RECENT_UPDATE_TIME,
                FDLFrameDelete.CONFIG_RECENT_UPDATE_TIME
            )

            config.edit().apply{
                putBoolean(CONFIG_RESET_UPDATE_TIME_V27, true)
                for(configKey in configKeyList){
                    putLong(configKey, 0L)
                }
            }.apply()
        }

        if(!config.getBoolean(CONFIG_RESET_THEME_UPDATE_TIME_V21, false)){
            config.edit().apply{
                putBoolean(CONFIG_RESET_THEME_UPDATE_TIME_V21, true)
                putLong(FDLTheme.CONFIG_RECENT_UPDATE_TIME, 0L)
            }.apply()
        }
    }

    private fun loadRecommend(){
        viewModelScope.launch {
            recommend.value = repository.getThemeRecommend()
        }
    }

    private fun waitTime(){
        viewModelScope.launch {
            delay(2000)
            isTimeOut.value = true
        }
    }

    private fun initResource(){
        viewModelScope.launch {
            repository.load(getApplication())

            val theme = repository.getThemeRecommendDownloadResource()
            if(theme != null){
                recommendDownload(theme)
            }

            isLoadComplete.value = true
        }
    }

    private suspend fun recommendDownload(theme: FThemeEntity){
        val preloadJob = viewModelScope.launch {
            if(theme.existPreload){
                return@launch
            }

            resourceDownload(theme.getPreloadProvider(getApplication()))

            theme.existPreload = true
            repository.updatedThemeResourcePreload(theme.id)
        }

        val themeJob = viewModelScope.launch {
            if(theme.existTheme){
                return@launch
            }

            resourceDownload(theme.getThemeProvider(getApplication()))

            theme.existTheme = true
            repository.updatedThemeResourceTheme(theme.id)
        }

        preloadJob.join()
        themeJob.join()
    }

    companion object{
        private const val CONFIG_RESET_UPDATE_TIME_V27 = "Config.ResetUpdateTime.V27"

        private const val CONFIG_RESET_THEME_UPDATE_TIME_V21 = "Config.ResetThemeUpdateTime.V21"
    }
}
