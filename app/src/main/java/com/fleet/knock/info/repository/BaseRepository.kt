package com.fleet.knock.info.repository

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.utils.dataloader.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*

open class BaseRepository constructor(application: Application) {
    protected val db = KNOCKDatabase.get(application)
    protected val fdb = FirebaseFirestore.getInstance()

    protected val job = Job()
    protected val scope = CoroutineScope(Dispatchers.IO + job)

    protected val config = PreferenceManager.getDefaultSharedPreferences(application)

    protected fun getDate(key:String, defaultValue:Long = 0L) = Date(config.getLong(key, defaultValue))

    protected fun setDate(key:String, setValue:Long = System.currentTimeMillis()){
        config.edit().apply{
            putLong(key, setValue)
            apply()
        }
    }

    private val fdlList = listOf<FDLoader<*>>(
        FDLUser(),
        FDLUserDelete(),
        FDLTheme(),
        FDLThemeDelete(),
        FDLRecommend(),
        FDLRecommendDelete(),
        FDLPromotion(),
        FDLPromotionDelete(),
        FDLProject(),
        FDLProjectDelete(),
        FDLFrame(),
        FDLFrameDelete()
    )

    suspend fun load(context: Context) = withContext(Dispatchers.IO + job){
        val jobs = mutableListOf<Job>()

        for (fdl in fdlList) {
            val job = scope.launch {
                fdl.load(context, fdb, db, config)
            }
            jobs.add(job)
        }

        for(job in jobs){
            job.join()
        }
    }
}