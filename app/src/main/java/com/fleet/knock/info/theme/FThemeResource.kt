package com.fleet.knock.info.theme

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.fleet.knock.info.provider.ImageProvider
import com.fleet.knock.info.provider.VideoProvider
import java.text.SimpleDateFormat
import java.util.*

data class FThemeResource(@ColumnInfo(name="theme_resource_thumbnail_file_name") var thumbnailFileName:String,
                          @ColumnInfo(name="theme_resource_update_thumbnail") var updateThumbnail:Boolean,
                          @ColumnInfo(name="theme_resource_update_thumbnail_time") var updateThumbnailTime: Date,
                          @ColumnInfo(name="theme_resource_preload_file_name") var preloadFileName:String,
                          @ColumnInfo(name="theme_resource_update_preload") var updatePreload:Boolean,
                          @ColumnInfo(name="theme_resource_theme_file_name") var themeFileName:String,
                          @ColumnInfo(name="theme_resource_update_theme") var updateTheme:Boolean,
                          @ColumnInfo(name="theme_resource_offering_screen") val offeringScreen: HashSet<String>
){
    constructor(id:String,
                thumbnailExt:String,
                updateThumbnailTime:Date,
                preloadExt:String,
                videoExt:String,
                offeringScreen: HashSet<String>):this("${id}.${thumbnailExt}",
        false,
        updateThumbnailTime,
        "${id}.${preloadExt}",
        false,
        "${id}.${videoExt}",
        false,
        offeringScreen)

    // 로컬에서 생성시 사용
    constructor(registeredTime:Date, formatter:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.KOREAN)) : this(
        "${formatter.format(registeredTime)}.jpg",
        true,
        registeredTime,
        "${formatter.format(registeredTime)}.jpg",
        true,
        "${formatter.format(registeredTime)}.mp4",
        true,
        hashSetOf(SCREEN_NONE))

    @Ignore
    private var _screenSize:String? = null
    private fun getScreenSize(context: Context):String{
        if(_screenSize == null) {
            _screenSize = if(offeringScreen.contains(SCREEN_NONE)){ SCREEN_NONE }
            else{
                val dm = context.resources.displayMetrics
                if(dm.heightPixels.toDouble() / dm.widthPixels >= 1.9){
                    SCREEN_LARGE
                }
                else{
                    SCREEN_NORMAL
                }
            }
        }
        return _screenSize ?: SCREEN_NONE
    }

    @Ignore
    private var _thumbnail: ImageProvider? = null
    fun getThumbnail(context: Context) = _thumbnail ?: ImageProvider(context,
        DIR_THEME,
        DIR_THUMBNAIL,
        getScreenSize(context),
        thumbnailFileName).also {
        _thumbnail = it
    }

    @Ignore
    private var _preload: ImageProvider? = null
    fun getPreload(context: Context) = _preload ?: ImageProvider(context,
        DIR_THEME,
        DIR_PRELOAD,
        getScreenSize(context),
        preloadFileName).also {
        _preload = it
    }

    @Ignore
    private var _theme: VideoProvider? = null
    fun getTheme(context: Context) = _theme ?: VideoProvider(context,
        DIR_THEME,
        DIR_VIDEO,
        getScreenSize(context),
        themeFileName).also {
        _theme = it
    }

    fun update(resource:FThemeResource){
        thumbnailFileName = resource.thumbnailFileName
        if(updateThumbnailTime < resource.updateThumbnailTime){
            updateThumbnail = false
            updateThumbnailTime = resource.updateThumbnailTime

        }

        preloadFileName = resource.preloadFileName
        updatePreload = false

        themeFileName = resource.themeFileName
        updateTheme = false
    }

    fun deleteFile(context:Context){
        getThumbnail(context).deleteFile()
        getPreload(context).deleteFile()
        getTheme(context).deleteFile()
    }

    companion object{
        @Ignore const val DIR_THEME = "theme"
        @Ignore const val DIR_THUMBNAIL = "thumbnail"
        @Ignore const val DIR_PRELOAD = "preload"
        @Ignore const val DIR_VIDEO = "video"

        @Ignore const val SCREEN_LARGE = "large"
        @Ignore const val SCREEN_NORMAL = "normal"
        @Ignore const val SCREEN_NONE = "none"
    }
}