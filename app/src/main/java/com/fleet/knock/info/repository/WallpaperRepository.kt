package com.fleet.knock.info.repository

import android.app.Application
import androidx.lifecycle.Transformations
import com.fleet.knock.info.user.UserConfig

class WallpaperRepository private constructor(application: Application) : UserRepository(application) {
    private val themeDao = db.themeDao()
    private val themeLocalDao = db.themeLocalDao()

    suspend fun getTheme(themeId:String) = themeDao.getSync(themeId)

    suspend fun getTheme(themeId:Long) = themeLocalDao.getSync(currentUserUid, themeId)

    companion object{
        private var INSTANCE: WallpaperRepository? = null
        fun get(application: Application) = INSTANCE ?: synchronized(this) {
            WallpaperRepository(application).also {
                INSTANCE = it
            }
        }
    }
}