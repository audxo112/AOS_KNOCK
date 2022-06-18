package com.fleet.knock.info.repository

import android.app.Application
import android.content.Context
import com.fleet.knock.info.theme.FThemeLocal
import kotlinx.coroutines.launch

class FThemeLocalRepository private constructor(application: Application) : UserRepository(application){
    private val themeLocalDao = db.themeLocalDao()

    fun getThemeAll() = themeLocalDao.getAll(currentUserUid)

    suspend fun getThemeAllSync() = themeLocalDao.getAllSync(currentUserUid)

    fun getThemeLimit(limit:Int) = themeLocalDao.getLimit(currentUserUid, limit)

    suspend fun getTheme(themeId:Long) = themeLocalDao.getSync(currentUserUid, themeId)

    suspend fun insertTheme(local: FThemeLocal) = themeLocalDao.insert(local)

    suspend fun updateSavedFileName(themeId:Long, fileName:String) = themeLocalDao.updateSavedFileName(themeId, fileName)

    fun deleteTheme(context: Context, themeId:Long){
        scope.launch {
            val local = themeLocalDao.getSync(currentUserUid, themeId)
            local ?: return@launch
            local.id ?: return@launch

            val config = userConfigDao.getSync(currentUserUid)
            config ?: return@launch

            if(config.isSelectedTheme(local.id)){
                removeSelectTheme()
            }

            local.deleteFile(context)

            themeLocalDao.delete(themeId)
        }
    }

    companion object{
        private var INSTANCE: FThemeLocalRepository? = null
        fun get(application: Application) = INSTANCE ?: synchronized(this){
            FThemeLocalRepository(application).also{
                INSTANCE = it
            }
        }
    }
}