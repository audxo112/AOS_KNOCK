package com.fleet.knock.ui.page.editor

import android.app.Application
import android.graphics.Point
import android.graphics.Typeface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.fleet.knock.R
import com.fleet.knock.info.editor.BaseThemeFrame
import com.fleet.knock.info.editor.ThemeFrame
import com.fleet.knock.info.repository.FrameRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashSet

class EditorFrameViewModel(application: Application,
                           screenSize: Point) : BaseViewModel(application){
    private val repository = FrameRepository.get(application)

    val baseFrameList = listOf(
        BaseThemeFrame(),
        BaseThemeFrame(screenSize,"정사각형-가운데정렬-좌우여백있음",
            R.drawable.ic_editor_tool_frame_1_selected,
            R.drawable.ic_editor_tool_frame_1_unselected,
            "1:1", 0, 0, dpToPixel(application,50), dpToPixel(application,50)),
        BaseThemeFrame(screenSize,"세로직사각형-가운데정렬 - 좌우여백있음",
            R.drawable.ic_editor_tool_frame_2_selected,
            R.drawable.ic_editor_tool_frame_2_unselected,
            "2:3", 0, 0, dpToPixel(application,60), dpToPixel(application,60)),
        BaseThemeFrame(screenSize,"가로직사각형-가운데정렬-좌우 여백없음",
            R.drawable.ic_editor_tool_frame_3_selected,
            R.drawable.ic_editor_tool_frame_3_unselected,
            "9:5", 0, 0, 0, 0),
        BaseThemeFrame(screenSize,"가로직사각형-가운데정렬-좌우 여백있음",
            R.drawable.ic_editor_tool_frame_4_selected,
            R.drawable.ic_editor_tool_frame_4_unselected,
            "2:1", 0, 0, dpToPixel(application,40), dpToPixel(application,40)),
        BaseThemeFrame(screenSize,"작은-세로직사각형-가운데정렬",
            R.drawable.ic_editor_tool_frame_5_selected,
            R.drawable.ic_editor_tool_frame_5_unselected,
            "9:14", 0, 0, dpToPixel(application,90), dpToPixel(application,90)),
        BaseThemeFrame(screenSize,"큰-세로직사각형-가운데정렬",
            R.drawable.ic_editor_tool_frame_6_selected,
            R.drawable.ic_editor_tool_frame_6_unselected,
            "1:2", 0, 0, dpToPixel(application,60), dpToPixel(application,60)),
        BaseThemeFrame(screenSize,"정사각형-아래정렬-120dp",
            R.drawable.ic_editor_tool_frame_7_selected,
            R.drawable.ic_editor_tool_frame_7_unselected,
            "1:1", 0, 0, dpToPixel(application,50), dpToPixel(application,50), 0.68f),
        BaseThemeFrame(screenSize,"정사각형-가운데정렬-좌우여백없음",
            R.drawable.ic_editor_tool_frame_8_selected,
            R.drawable.ic_editor_tool_frame_8_unselected,
            "1:1", 0, 0, 0, 0),
        BaseThemeFrame(screenSize,"정사각형-위정렬-60dp",
            R.drawable.ic_editor_tool_frame_9_selected,
            R.drawable.ic_editor_tool_frame_9_unselected,
            "1:1", 0, dpToPixel(application,130), dpToPixel(application,50), dpToPixel(application,50), 0.16f),
        BaseThemeFrame(screenSize,"가로직사각형-아래정렬-160dp",
            R.drawable.ic_editor_tool_frame_10_selected,
            R.drawable.ic_editor_tool_frame_10_unselected,
            "9:5", 0, 0, 0, 0, 0.64f),
        BaseThemeFrame(screenSize,"가로직사각형-위정렬-120dp",
            R.drawable.ic_editor_tool_frame_11_selected,
            R.drawable.ic_editor_tool_frame_11_unselected,
            "9:5", 0, dpToPixel(application,100), 0, 0, 0.27f)
    )

    val selectedFrameIndex = MutableLiveData(0)

    private val frameType = Transformations.map(selectedFrameIndex){
        if(it < baseFrameList.size) FRAME_TYPE_BASE
        else FRAME_TYPE_TEMPLATE
    }

    val baseFrameIndex = 0
    val templateFrameIndex:Int
        get() =
            if(templateList == null || templateList?.isEmpty() == true) baseFrameList.size - 1
            else baseFrameList.size

    private var templateList:List<ThemeFrame>? = null

    suspend fun getTemplateFrameList() = templateList ?: repository.getFrameAllSync().also{
        templateList = it
    }

    private val existConfirmFrame = repository.existConfirmFrame()

    val visibilityTemplateIndicator = Transformations.map(existConfirmFrame){
        if(it == null) View.INVISIBLE
        else View.VISIBLE
    }

    val frameBaseTextStyle = Transformations.map(frameType){
        if(it == FRAME_TYPE_BASE) Typeface.BOLD
        else Typeface.NORMAL
    }

    val frameBaseTextColor = Transformations.map(frameType){
        if(it == FRAME_TYPE_BASE) R.color.colorTextWhite
        else R.color.colorTextGray
    }

    val frameBaseTextBackground = Transformations.map(frameType){
        if(it == FRAME_TYPE_BASE) R.drawable.bg_dark_gray3_r19_box
        else R.drawable.ripple_gray_a15_r19_box
    }

    val frameTemplateTextStyle = Transformations.map(frameType){
        if(it == FRAME_TYPE_TEMPLATE) Typeface.BOLD
        else Typeface.NORMAL
    }

    val frameTemplateTextColor = Transformations.map(frameType){
        if(it == FRAME_TYPE_TEMPLATE) R.color.colorTextWhite
        else R.color.colorTextGray
    }

    val frameTemplateTextBackground = Transformations.map(frameType){
        if(it == FRAME_TYPE_TEMPLATE) R.drawable.bg_dark_gray3_r19_box
        else R.drawable.ripple_gray_a15_r19_box
    }

    suspend fun miniThumbnailDownload(frame: ThemeFrame){
        if(frame.miniThumbnailUpdate)
            return

        resourceDownload(frame.getMiniThumbnail(getApplication()))

        frame.miniThumbnailUpdate = true
        repository.updateFrameMiniThumbnail(frame.id)
    }

    suspend fun thumbnailDownload(frame:ThemeFrame){
        if(frame.thumbnailUpdate)
            return

        resourceDownload(frame.getThumbnail(getApplication()))

        frame.thumbnailUpdate = true
        repository.updateFrameThumbnail(frame.id)
    }

    suspend fun frameDownload(frame:ThemeFrame){
        if(frame.frameUpdate)
            return

        resourceDownload(frame.getFrame(getApplication()))

        frame.frameUpdate = true
        repository.updateFrameFrame(frame.id)
    }

    fun updateRecentUsedTime(frame:ThemeFrame?){
        frame?:return
        viewModelScope.launch {
            val time = Date()
            frame.recentUsedTime = time
            repository.updateFrameRecentUsed(frame.id, time)
        }
    }

    class EditorFrameViewModelFactory(private val application: Application,
                                      private val screenSize: Point) : ViewModelProvider.Factory{

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditorFrameViewModel(application, screenSize) as T
        }
    }

    companion object{
        fun new(activity: AppCompatActivity,
                screenSize: Point) = ViewModelProvider(activity, EditorFrameViewModelFactory(
            activity.application,
            screenSize
        )).get(EditorFrameViewModel::class.java)

        const val FRAME_TYPE_BASE = "FrameType.Base"
        const val FRAME_TYPE_TEMPLATE = "FrameType.Template"
    }
}