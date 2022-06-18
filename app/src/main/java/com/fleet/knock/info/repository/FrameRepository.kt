package com.fleet.knock.info.repository

import android.app.Application
import java.util.*

class FrameRepository private constructor(application: Application) : BaseRepository(application){
    private val frameDao = db.themeFrameDao()

    suspend fun getFrameAllSync() = frameDao.getAllSync()

    fun existConfirmFrame() = frameDao.existConfirm(Date())

    suspend fun updateFrameConfirmTime(id:String, time:Date = Date()) = frameDao.updateConfirmTime(id, time)

    suspend fun updateFrameRecentUsed(id:String, time: Date = Date()) = frameDao.updateRecentUsed(id, time)

    suspend fun updateFrameMiniThumbnail(id:String) = frameDao.updateMiniThumbnail(id)

    suspend fun updateFrameThumbnail(id:String) = frameDao.updateThumbnail(id)

    suspend fun updateFrameFrame(id:String) = frameDao.updateFrame(id)

    companion object{
        private var INSTANCE: FrameRepository? = null
        fun get(application:Application) = INSTANCE ?: synchronized(this){
            FrameRepository(application).also{
                INSTANCE = it
            }
        }
    }
}