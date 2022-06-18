package com.fleet.knock.utils

import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.fleet.knock.wallpaper.ExoPlayerWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.NullPointerException

object WallpaperUtil {
    // Live Wallpaper 설정을 요철
    const val REQUEST_SET_WALLPAPER = 3001

    // 테마적용 까지 요청
    const val REQUEST_SET_THEME = 3002

    fun isSetWallpaper(context: Context) : Boolean{
        return try {
            WallpaperManager.getInstance(context).wallpaperInfo.let {
                it != null && it.serviceName == context.packageName + ".wallpaper.ExoPlayerWallpaper"
            }
        } catch (e:NullPointerException){
            e.printStackTrace()
            false
        }
    }

    fun setWallpaper(activity: Activity, requestCode:Int){
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply{
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(activity, ExoPlayerWallpaper::class.java)
            )
        }
        activity.startActivityForResult(intent, requestCode)
    }

    fun removeWallpaper(activity:Activity?, onProgress:(progress:Boolean)->Unit = {}, onComplete:()->Unit = {}) {
        activity?:return
        onProgress(true)
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                WallpaperManager.getInstance(activity).clear()
            }

            if(activity.isFinishing){
                return@launch
            }

            onProgress(false)
            onComplete()
        }
    }
}