package com.fleet.knock.info.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Size
import com.fleet.knock.utils.UriUtil
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.io.IOException

data class Video(val id:Long,
                 private var _uri: Uri?,
                 val contentUri:Uri,
                 private var _thumbnail: Bitmap?,
                 private var _duration:Long?,
                 private val ratio:Float) {
    val durationStr
        get() = StringBuilder().apply {
            val d = _duration?: 0L
            val date = d / 86400000
            val hour = d % 86400000 / 3600000
            val minute = d % 3600000 / 60000
            val second = d % 60000 / 1000

            if (date > 0) append("$date:")
            if (date > 0 || hour > 0) {
                if (hour >= 10) append(String.format("%02d:", hour))
                else append(String.format("%d:", hour))
            }
            if (minute >= 10) append(String.format("%02d:%02d", minute, second))
            else append(String.format("%d:%02d", minute, second))
        }.toString()

    private fun dpToPixel(context:Context, dp:Int) = context.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun getVideoThumbnail(context:Context) : Bitmap? {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val size = context.resources.displayMetrics.let { dm ->
                (dm.widthPixels - dpToPixel(context, 6)) / 3
            }
            return try {
                context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(size, size),
                    null
                )
            }catch (e:ImageDecoder.DecodeException){
                null
            }catch (e: IOException){
                null
            }
        }
        else{
            ThumbnailUtils.createVideoThumbnail(
                getUri(context)?.path ?: "",
                MediaStore.Video.Thumbnails.MINI_KIND)
        }
    }

    fun getThumbnail(context: Context) = _thumbnail ?: getVideoThumbnail(context).also{
        _thumbnail = it
    }

    fun getDuration(context : Context) = _duration ?: getVideoDuration(context).also{
        _duration = it
    }

    fun getDurationStr(context:Context) : String{
        if(_duration == null){
            _duration = getVideoDuration(context)
        }
        return durationStr
    }

    private fun getVideoDuration(context:Context) : Long{
        val uri = getUri(context) ?: return 0L
        return try {
            val mr = MediaMetadataRetriever()
            mr.setDataSource(context, uri)
            val duration =
                mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            mr.release()
            duration
        }
        catch(e:Exception){
            0L
        }
    }

    fun getUri(context:Context) = _uri ?: UriUtil.getUri(context, contentUri).also{
        _uri = it
    }

    private var _resizeMode:Int? = null
    fun getResizeMode(context:Context) = _resizeMode ?: getVideoResizeMode(context).also{
        _resizeMode = it
    }

    private fun getVideoResizeMode(context:Context) : Int {
        val uri = getUri(context) ?: return AspectRatioFrameLayout.RESIZE_MODE_FIT
        val rotation = try {
            val mr = MediaMetadataRetriever()
            mr.setDataSource(context, uri)
            val rotation = mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0
            mr.release()
            rotation
        }
        catch(e:Exception){
            0
        }

        return if(rotation / 90 == 1){
            if(ratio <= 0.8) AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        else{
            if(ratio >= 1.3) AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }
}