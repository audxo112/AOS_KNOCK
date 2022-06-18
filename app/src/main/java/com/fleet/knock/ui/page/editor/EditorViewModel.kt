package com.fleet.knock.ui.page.editor

import android.app.Application
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeLocalRepository
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.ui.view.EditorPlayerView
import com.fleet.knock.ui.view.FPlayerView
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.StorageUtil
import com.fleet.knock.utils.encoding.GifEncodingJob
import com.fleet.knock.utils.encoding.LocalThemeEncodingJob
import com.fleet.knock.utils.tutorial.TutorialEditorFrame
import com.fleet.knock.utils.tutorial.TutorialEditorTemplate
import com.fleet.knock.utils.tutorial.TutorialUtil
import com.fleet.knock.utils.viewmodel.BaseViewModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.auth.api.signin.internal.Storage
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class EditorViewModel(
    application: Application,
    screenSize:Point,
    private val uri:Uri,
    private val srcType:String,
    private val duration:Long
) : BaseViewModel(application){
    val repository = FThemeLocalRepository.get(application)

    fun logApplyLocalTheme(){
        GAUtil.get(getApplication())
            .logApplyLocalTheme(
                GAUtil.APPLY_THEME_POS_EDIT
            )
    }

    fun logEncoding(bundle: Bundle) {
        GAUtil.get(getApplication())
            .logEncoding(
                bundle.apply{
                    putString("종류",
                        if(srcType.contains("Gif")) GAUtil.ENCODING_TYPE_GIF
                        else GAUtil.ENCODING_TYPE_VIDEO
                    )
                    putBoolean("부메랑", boomerang.value == true)
                }
            )
    }

    val simplePlayer = SimpleExoPlayer.Builder(application).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        volume = 0.0f

        addListener(object: Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                videoPlayWhenReady.value = playWhenReady
            }
        })
    }

    val currentDate = MutableLiveData<String>()
    val currentTime = MutableLiveData<String>()

    private val current = Date()
    private val dateFormat = SimpleDateFormat("M월 d일 E요일", Locale.KOREAN)
    private val timeFormat = SimpleDateFormat("h:mm", Locale.KOREAN)

    private var clock: Job? = null

    private fun startClock(){
        clock = viewModelScope.launch(Dispatchers.Default){
            while(isActive){
                current.time = System.currentTimeMillis()

                val date = dateFormat.format(current)
                if(currentDate.value != date)
                    currentDate.postValue(date)

                val time = timeFormat.format(current)
                if(currentTime.value != time)
                    currentTime.postValue(time)

                delay(60000L - current.time % 60000L)
            }
        }
    }

    private fun stopClock(){
        clock?.cancel()
    }

    private val videoPlayWhenReady = MutableLiveData<Boolean>()
    val isPlayIcon = Transformations.map(videoPlayWhenReady){ isPlay->
        if(isPlay) ContextCompat.getDrawable(application,R.drawable.ic_pause)
        else ContextCompat.getDrawable(application,R.drawable.ic_play)
    }

    val isTooLong
        get() = duration > 30000L

    val seekBarVisibility
        get() = if(isTooLong) View.VISIBLE
            else View.GONE

    private val defaultPage =
        if(isTooLong) PAGE_WARNING_TOO_LONG
        else PAGE_EDITOR

    private var currentPage = MutableLiveData(defaultPage)

    fun isSamePage(page:String) = currentPage.value == page

    fun previewPage(page:String = currentPage.value ?: PAGE_COMPLETE) = when(page){
        PAGE_COMPLETE -> PAGE_COMPLETE_HOME_PREVIEW
        PAGE_COMPLETE_HOME_PREVIEW -> PAGE_COMPLETE_LOCK_PREVIEW
        PAGE_COMPLETE_LOCK_PREVIEW -> PAGE_COMPLETE
        else -> PAGE_COMPLETE
    }

    val homeScreenGradientColor = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW -> ContextCompat.getColor(application, R.color.colorBackgroundWhite)
            PAGE_COMPLETE_LOCK_PREVIEW -> ContextCompat.getColor(application, R.color.colorBackgroundBlackA10)
            else -> ContextCompat.getColor(application, R.color.colorTransparent)
        }
    }

    val homeScreenTextColor = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW -> ContextCompat.getColor(application, R.color.colorTextBlack)
            PAGE_COMPLETE_LOCK_PREVIEW -> ContextCompat.getColor(application, R.color.colorTextWhite)
            else -> ContextCompat.getColor(application, R.color.colorTransparent)
        }
    }

    val lockScreenGradientColor = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW -> ContextCompat.getColor(application, R.color.colorBackgroundBlackA10)
            PAGE_COMPLETE_LOCK_PREVIEW -> ContextCompat.getColor(application, R.color.colorBackgroundWhite)
            else -> ContextCompat.getColor(application, R.color.colorTransparent)
        }
    }

    val lockScreenTextColor = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW -> ContextCompat.getColor(application, R.color.colorTextWhite)
            PAGE_COMPLETE_LOCK_PREVIEW -> ContextCompat.getColor(application, R.color.colorTextBlack)
            else -> ContextCompat.getColor(application, R.color.colorTransparent)
        }
    }

    val visibilityLockScreen = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW,
            PAGE_COMPLETE_LOCK_PREVIEW -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    val visibilityHomeScreen = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW,
            PAGE_COMPLETE_LOCK_PREVIEW -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    val visibilityLockPreview = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_LOCK_PREVIEW -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    val visibilityHomePreview = Transformations.map(currentPage){
        when(it){
            PAGE_COMPLETE_HOME_PREVIEW -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    private var currentTool = MutableLiveData(TOOL_FRAME)

    val controllerPos = Transformations.map(currentTool){
        when(it){
            TOOL_TRIM->if(isTooLong) 124
                else  88
            TOOL_BG_COLOR -> 88
            TOOL_FRAME-> 124
            else -> 124
        }
    }

    val visibilityFrame = Transformations.map(currentTool){
        if(it == TOOL_FRAME) View.VISIBLE
        else View.INVISIBLE
    }

    val navFrameIcon = Transformations.map(currentTool){
        if(it == TOOL_FRAME) ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_frame_selected)
        else ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_frame_unselected)
    }

    val navFrameBackground = Transformations.map(currentTool){
        if(it == TOOL_FRAME) ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_white_circle)
        else ContextCompat.getDrawable(application,R.drawable.ripple_gray_a30_dark_gray_circle)
    }

//    var needTutorialFrameTemplate
//        get() = TutorialUtil.get().need(getApplication(), TutorialEditorTemplate.NAME)
//        set(value){
//            if(!value) {
//                TutorialUtil.get().complete(getApplication(), TutorialEditorTemplate.NAME)
//                visibilityFrameTemplateIndicate.value = View.INVISIBLE
//            }
//        }
//
//    val visibilityFrameTemplateIndicate = MutableLiveData(
//        if(needTutorialFrameTemplate) View.VISIBLE
//        else View.INVISIBLE
//    )

    val visibilityTrim = Transformations.map(currentTool){
        if(it == TOOL_TRIM) View.VISIBLE
        else View.INVISIBLE
    }

    val navTrimBackground = Transformations.map(currentTool){
        if(it == TOOL_TRIM) ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_white_circle)
        else ContextCompat.getDrawable(application,R.drawable.ripple_gray_a30_dark_gray_circle)
    }

    val navTrimIcon = Transformations.map(currentTool){
        if(it == TOOL_TRIM) ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_trim_selected)
        else ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_trim_unselected)
    }

    val visibilityBgColor = Transformations.map(currentTool){
        if(it == TOOL_BG_COLOR) View.VISIBLE
        else View.INVISIBLE
    }

    val navBgColorIcon = Transformations.map(currentTool){
        if(it == TOOL_BG_COLOR) ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_bg_color_selected)
        else ContextCompat.getDrawable(application,R.drawable.ic_editor_tool_bg_color_unselected)
    }

    val navBgColorBackground = Transformations.map(currentTool){
        if(it == TOOL_BG_COLOR) ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_white_circle)
        else ContextCompat.getDrawable(application,R.drawable.ripple_gray_a30_dark_gray_circle)
    }

    fun toolTargetId(tool:String) = when(tool){
        TOOL_FRAME -> R.id.tool_frame
        TOOL_TRIM -> R.id.tool_trim
        TOOL_BG_COLOR -> R.id.tool_bg_color
        else -> R.id.tool_frame
    }

    fun isSameTool(tool:String) = currentTool.value == tool

    fun applyTool(tool:String){
        currentTool.value = tool

        simplePlayer.repeatMode = repeatMode
    }

    private val repeatMode
        get() = if(currentPage.value == PAGE_EDITOR && currentTool.value == TOOL_TRIM){
            Player.REPEAT_MODE_OFF
        }
        else{
            Player.REPEAT_MODE_ONE
        }

    fun videoChangePage(targetPage:String) : Boolean{
        return when(currentPage.value){
            PAGE_EDITOR -> targetPage == PAGE_COMPLETE
            PAGE_COMPLETE,
            PAGE_COMPLETE_HOME_PREVIEW,
            PAGE_COMPLETE_LOCK_PREVIEW -> targetPage == PAGE_EDITOR
            else -> false
        }
    }

    fun applyPage(page:String){
        if(page == PAGE_COMPLETE_LOCK_PREVIEW)
            startClock()
        else
            stopClock()

        currentPage.value = page

        simplePlayer.repeatMode = repeatMode
    }

    fun mode(page:String = currentPage.value ?: defaultPage, tool:String = currentTool.value ?: TOOL_FRAME) = when(page){
        PAGE_EDITOR -> when(tool){
            TOOL_FRAME,
            TOOL_BG_COLOR-> FPlayerView.MODE_RESIZE
            TOOL_TRIM -> FPlayerView.MODE_PLAY
            else -> EditorPlayerView.MODE_NONE
        }
        PAGE_WARNING_TOO_LONG ,
        PAGE_COMPLETE -> EditorPlayerView.MODE_NONE
        else -> EditorPlayerView.MODE_NONE
    }

    val visibilityTrimHelp = MediatorLiveData<Int>().apply{
        addSource(currentPage){
            visibilityTrimHelp(it, currentTool.value)
        }
        addSource(currentTool){
            visibilityTrimHelp(currentPage.value, it)
        }
    }

    private fun visibilityTrimHelp(currentPage:String?, currentTool:String?){
        currentPage ?: return
        currentTool ?: return

        visibilityTrimHelp.value =
            if(currentPage == PAGE_EDITOR && currentTool == TOOL_TRIM) View.VISIBLE
            else View.INVISIBLE
    }

    fun backAction(page:String = currentPage.value ?: defaultPage) = when(page){
        PAGE_WARNING_TOO_LONG ,
        PAGE_EDITOR -> ACTION_GOTO_GALLERY
        PAGE_COMPLETE -> ACTION_SHOW_REVERT_EDITOR_DIALOG
        PAGE_COMPLETE_HOME_PREVIEW,
        PAGE_COMPLETE_LOCK_PREVIEW-> ACTION_GOTO_COMPLETE
        else -> ACTION_GOTO_GALLERY
    }

    val isLoading = MutableLiveData(false)
    val visibilityLoading = Transformations.map(isLoading){
        if(it) View.VISIBLE
        else View.INVISIBLE
    }

    val boomerang = MutableLiveData(false)
    val boomerangIcon = Transformations.map(boomerang){
        if(it) ContextCompat.getDrawable(application,R.drawable.ic_boomerang_on)
        else ContextCompat.getDrawable(application,R.drawable.ic_boomerang_off)
    }
    val boomerangBackground = Transformations.map(boomerang){
        if(it) ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_white_circle)
        else ContextCompat.getDrawable(application,R.drawable.ripple_gray_a30_dark_gray_circle)
    }

    val screenRatio = "W,${screenSize.x}:${screenSize.y}"

    private var gifEncodingJob:GifEncodingJob? = null
    private var encodingJob:LocalThemeEncodingJob? = null

    private var editVideoUri:Uri? = null
    fun getEditVideoUri() = editVideoUri

    suspend fun makeEditVideoUri() : Uri?{
        return if(srcType == EditorActivity.TYPE_VIDEO) uri
        else{
            isLoading.postValue(true)
            gifEncodingJob?.cancel()

            gifEncodingJob = GifEncodingJob()
            gifEncodingJob?.execute(getApplication(), uri).also{
                isLoading.postValue(false)
            }
        }.also{
            editVideoUri = it
        }
    }

    private var themeLocal:FThemeLocal? = null
    fun getThemeLocal() = themeLocal ?: FThemeLocal(repository.currentUserUid, Date()).also{
        themeLocal = it
    }

    val existSavedFile = MutableLiveData<Boolean>(false)
    val savedFileNameDrawable = Transformations.map(existSavedFile){
        if(it) ContextCompat.getDrawable(application,R.drawable.bg_dark_gray_r6_box)
        else ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_r6_white_box)
    }
    val savedFileNameSrc = Transformations.map(existSavedFile){
        if(it) ContextCompat.getDrawable(application,R.drawable.ic_save_on_device_exist)
        else ContextCompat.getDrawable(application,R.drawable.ic_save_on_device)
    }

    suspend fun saveTheme() : Boolean{
        val themeId = themeLocal?.id ?: return false
        val provider = themeLocal?.getThemeProvider(getApplication())
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss_SSS'.mp4'", Locale.KOREAN).format(Date())

        val result = withContext(Dispatchers.IO){
            StorageUtil.saveVideo(getApplication<Application>().contentResolver, provider?.file, fileName)
        }

        if(result){
            GAUtil.get(getApplication()).logDownloadLocalTheme()
            themeLocal?.savedFileName = fileName
            existSavedFile.value = true
            repository.updateSavedFileName(themeId, fileName)
        }

        return result
    }

    fun deleteTheme(){
        val themeId = themeLocal?.id ?: return

        StorageUtil.deleteFile(getApplication<Application>().contentResolver, themeLocal?.savedFileName ?: "")

        viewModelScope.launch {
            themeLocal?.savedFileName = ""
            existSavedFile.value = false
            repository.updateSavedFileName(themeId, "")
        }
    }

    suspend fun encodingTheme(filterComplex:String,
                              progressCallback:(progress:Int)->Unit) : Int{
        encodingJob?.cancel()

        val uri = editVideoUri ?: return LocalThemeEncodingJob.RESULT_FAILED
        val local = getThemeLocal()

        encodingJob = LocalThemeEncodingJob().apply{
            setProgressCallback(progressCallback)
        }

        val result = encodingJob?.execute(getApplication(),
            uri,
            local,
            filterComplex) ?: LocalThemeEncodingJob.RESULT_FAILED

        if(result == LocalThemeEncodingJob.RESULT_OK && local.id == null){
            repository.getTheme(repository.insertTheme(local)).also{
                themeLocal = it
            }
        }

        return result
    }

    fun encodingCancel(){
        encodingJob?.cancel()
    }

    fun restoreSelectTheme(){
        viewModelScope.launch{
            repository.restoreSelectTheme()
        }
    }

    fun selectLocalTheme(){
        val themeId = themeLocal?.id?:return
        viewModelScope.launch {
            repository.selectLocalTheme(themeId)
        }
    }

    override fun onCleared() {
        stopClock()

        gifEncodingJob?.cancel()
        encodingJob?.cancel()

        simplePlayer.stop()
        simplePlayer.release()
    }

    class EditorViewModelFactory(private val application:Application,
                                 private val screenSize:Point,
                                 private val uri:Uri,
                                 private val srcType:String,
                                 private val duration:Long) : ViewModelProvider.Factory{

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditorViewModel(application, screenSize, uri, srcType, duration) as T
        }
    }

    companion object {
        fun new(
            activity: AppCompatActivity,
            screenSize: Point,
            uri: Uri,
            srcType: String,
            duration: Long
        ) = ViewModelProvider(
            activity,
            EditorViewModelFactory(
                activity.application,
                screenSize,
                uri,
                srcType,
                duration
            )
        ).get(EditorViewModel::class.java)

        const val PAGE_WARNING_TOO_LONG = "Page.WarningTooLong"
        const val PAGE_EDITOR = "Page.Editor"
        const val PAGE_COMPLETE = "Page.Complete"
        const val PAGE_COMPLETE_LOCK_PREVIEW = "Page.Complete.LockPreview"
        const val PAGE_COMPLETE_HOME_PREVIEW = "Page.Complete.HomePreview"

        const val TOOL_FRAME = "Tool.Frame"
        const val TOOL_BG_COLOR = "Tool.BackgroundColor"
        const val TOOL_TRIM = "Tool.Trim"

        const val ACTION_GOTO_GALLERY = "Action.GoToGallery"
        const val ACTION_SHOW_REVERT_EDITOR_DIALOG = "Action.ShowRevertEditorDialog"
        const val ACTION_GOTO_COMPLETE = "Action.GoToComplete"
    }
}