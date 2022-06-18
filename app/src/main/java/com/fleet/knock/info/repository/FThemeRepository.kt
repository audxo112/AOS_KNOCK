package com.fleet.knock.info.repository

import android.app.Application
import android.content.Context
import com.fleet.knock.utils.dataloader.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FThemeRepository private constructor(application: Application) : UserRepository(application) {
    private val userDao = db.userDao()
    private val themeDao = db.themeDao()
    private val themeRecommendDao = db.themeRecommendDao()

    private val promotionDao = db.promotionDao()
    private val projectDao = db.projectDao()

    suspend fun updatedThemeResourceThumbnail(id: String) = themeDao.updateThumbnail(id)

    suspend fun updatedThemeResourcePreload(id: String) = themeDao.updatePreload(id)

    suspend fun updatedThemeResourceTheme(id: String) = themeDao.updateTheme(id)

    suspend fun updateUserAvatar(uid:String) = userDao.updateAvatar(uid)

    suspend fun getThemeRecommend() = themeRecommendDao.getSync()

    suspend fun getThemeRecommendDownloadResource() = themeRecommendDao.getDownloadResource()

    suspend fun getThemeSync(themeId:String) = themeDao.getSync(themeId)

    fun getPromotion(promotionId:String) = promotionDao.get(promotionId)

    suspend fun getPromotionAll() = promotionDao.getAllSync()

    suspend fun updatePromotionBanner(promotionId:String) = promotionDao.updateBanner(promotionId)

    suspend fun updatePromotionMain(promotionId:String) = promotionDao.updateMain(promotionId)

    fun getRecentThemeAll() = themeDao.getRecentAll(Date())

    fun getRecentThemeAllSync() = themeDao.getRecentAllSync(Date())

    fun getRecentThemeAllCount() = themeDao.getRecentAllCount(Date())

    suspend fun getProjectAll(promotionId:String) = projectDao.getAllWithThemeSync(promotionId, Date())

    suspend fun getThemeAllSync() = themeDao.getAllSync(Date())

    suspend fun getThemeAllInProjectSync(projectId:String) = themeDao.getAllInProjectSync(projectId, Date())

    suspend fun getThemeAllInPromotionSync(promotionId:String) = themeDao.getAllInPromotionSync(promotionId, Date())

    companion object {
        private var INSTANCE: FThemeRepository? = null
        fun get(application: Application) = INSTANCE ?: synchronized(this) {
            FThemeRepository(application).also {
                INSTANCE = it
            }
        }
    }
}
