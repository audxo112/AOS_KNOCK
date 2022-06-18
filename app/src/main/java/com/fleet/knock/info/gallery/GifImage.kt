package com.fleet.knock.info.gallery

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import com.fleet.knock.utils.UriUtil
import pl.droidsonroids.gif.GifDrawable
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.system.measureTimeMillis

data class GifImage(val id:Long,
                    private var _uri: Uri?,
                    val contentUri:Uri,
                    private var _thumbnail: Bitmap?,
                    private var _duration:Long?,
                    val resizeMode:Int){

    private fun dpToPixel(context: Context, dp:Int) = context.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun getGifThumbnail(context: Context) : Bitmap? {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val size = context.resources.displayMetrics.let { dm ->
                (dm.widthPixels - dpToPixel(context,6)) / 3
            }
            return try{
                context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(size, size),
                    null
                )
            }catch (e:ImageDecoder.DecodeException){
                null
            } catch (e:IOException){
                null
            }
        }
        else{
            MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null
            )

//            ThumbnailUtils.createImageThumbnail(
//                getUri(context)?.path ?: "",
//                MediaStore.Images.Thumbnails.MINI_KIND
//            )
        }
    }

    fun getThumbnail(context: Context) = _thumbnail ?: getGifThumbnail(context).also{
        _thumbnail = it
    }

    fun getDuration(context:Context) = _duration ?: getGifDuration(context).also{
        _duration = it
    }

    private fun getGifDuration(context:Context) : Long{
        val uri = getUri(context) ?: return 0L
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val path = uri.path ?: return 0L
                GifDrawable(path).duration.toLong()
            } else {
                Movie.decodeFile(uri.path)?.duration()?.toLong() ?: 0L
            }
        }
        catch (e: Exception){
            0L
        }
    }

    fun getUri(context:Context) = _uri ?: UriUtil.getUri(context, contentUri).also{
        _uri = it
    }
}
