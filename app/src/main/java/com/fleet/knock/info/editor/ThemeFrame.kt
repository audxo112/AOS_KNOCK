package com.fleet.knock.info.editor

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fleet.knock.info.provider.ImageProvider
import java.util.*

@Entity(tableName="theme_frame")
data class ThemeFrame(@PrimaryKey(autoGenerate = false) @ColumnInfo(name="theme_frame_id") val id:String,
                      @ColumnInfo(name="theme_frame_title") var title:String,
                      @ColumnInfo(name="theme_frame_type") var type:String,
                      @ColumnInfo(name="theme_frame_priority") var priority:Long,
                      @ColumnInfo(name="theme_frame_scale_type") var scaleType:String,
                      @ColumnInfo(name="theme_frame_repeat_mode") var repeatMode:String,
                      @ColumnInfo(name="theme_frame_offering_screen") var offeringScreen:HashSet<String>,
                      @ColumnInfo(name="theme_frame_mini_thumbnail_ext") var miniThumbnailExt:String,
                      @ColumnInfo(name="theme_frame_mini_thumbnail_update") var miniThumbnailUpdate:Boolean,
                      @ColumnInfo(name="theme_frame_thumbnail_ext") var thumbnailExt:String,
                      @ColumnInfo(name="theme_frame_thumbnail_update") var thumbnailUpdate:Boolean,
                      @ColumnInfo(name="theme_frame_frame_ext") var frameExt:String,
                      @ColumnInfo(name="theme_frame_frame_update") var frameUpdate:Boolean,
                      @ColumnInfo(name="theme_frame_update_time") var updateTime: Date,
                      @ColumnInfo(name="theme_frame_confirm_time") var confirmTime:Date,
                      @ColumnInfo(name="theme_frame_recent_used_time") var recentUsedTime:Date,
                      @ColumnInfo(name="theme_frame_registered_time") val registeredTime:Date){

    @Ignore
    private var _screenSize:String? = null
    private fun getScreenSize(context:Context) : String{
        if(_screenSize == null) {
            _screenSize = if(offeringScreen.contains(SCREEN_NONE)) SCREEN_NONE
            else{
                val dm = context.resources.displayMetrics
                if(dm.heightPixels.toDouble() / dm.widthPixels >= 1.9)
                    SCREEN_LARGE
                else SCREEN_NORMAL
            }
        }
        return _screenSize ?: SCREEN_NONE
    }

    @Ignore
    private var _miniThumbnail: ImageProvider? = null
    fun getMiniThumbnail(context:Context) = _miniThumbnail ?: ImageProvider(context,
        DIR_THEME_FRAME,
        id,
        "$DIR_MINI_THUMBNAIL.$miniThumbnailExt"
    ).also{
        _miniThumbnail = it
    }

    @Ignore
    private var _thumbnail: ImageProvider? = null
    fun getThumbnail(context: Context) = _thumbnail ?: ImageProvider(context,
        DIR_THEME_FRAME,
        id,
        "$DIR_THUMBNAIL.$thumbnailExt"
    ).also{
        _thumbnail = it
    }

    @Ignore
    private var _frame : ImageProvider? = null
    fun getFrame(context:Context) = _frame ?: ImageProvider(context,
        DIR_THEME_FRAME,
        id,
        getScreenSize(context),
        "$DIR_FRAME.$frameExt"
    ).also {
        _frame = it
    }

    fun update(frame:ThemeFrame){
        title = frame.title
        type = frame.type
        thumbnailExt = frame.thumbnailExt
        thumbnailUpdate = false
        frameExt = frame.frameExt
        frameUpdate = false
        scaleType = frame.scaleType
        repeatMode = frame.repeatMode
        offeringScreen.clear()
        offeringScreen.addAll(frame.offeringScreen)
        updateTime = frame.updateTime
    }

    fun deleteFile(context:Context){
        getThumbnail(context).deleteFile()
        getFrame(context).deleteFile()
    }

    companion object{
        @Ignore const val DIR_THEME_FRAME = "theme_frames"
        @Ignore const val DIR_MINI_THUMBNAIL = "mini_thumbnail"
        @Ignore const val DIR_THUMBNAIL = "thumbnail"
        @Ignore const val DIR_FRAME = "frame"

        @Ignore const val TYPE_GIF = "Gif"
        @Ignore const val TYPE_IMAGE = "Image"

        @Ignore const val SCALE_TYPE_SCALE = "scale"
        @Ignore const val SCALE_TYPE_CROP = "crop"

        @Ignore const val REPEAT_MODE_NONE = "none"
        @Ignore const val REPEAT_MODE_LOOP = "loop"
        @Ignore const val REPEAT_MODE_SYNC = "sync"
        @Ignore const val REPEAT_MODE_ONE_STOP = "stop"
        @Ignore const val REPEAT_MODE_ONE_TIME = "one_time"

        @Ignore const val SCREEN_LARGE = "large"
        @Ignore const val SCREEN_NORMAL = "normal"
        @Ignore const val SCREEN_NONE = "none"
    }
}