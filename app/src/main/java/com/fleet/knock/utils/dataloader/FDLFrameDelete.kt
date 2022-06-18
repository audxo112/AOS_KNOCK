package com.fleet.knock.utils.dataloader

import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.ThemeFrameDeleteMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FDLFrameDelete : FDLoader<String>(COL_NAME, ThemeFrameDeleteMapper()){
    override suspend fun load(
        context: Context,
        fdb: FirebaseFirestore,
        db: KNOCKDatabase,
        config: SharedPreferences
    ) {
        try{
            val query = fdb.collection(name)
                .whereGreaterThan(FIELD_UPDATE_TIME, getDate(config, CONFIG_RECENT_UPDATE_TIME))
                .get()
                .await()

            if(!query.isEmpty) {
                val frameDeleteDao = db.themeFrameDao()

                for (dc in query.documentChanges) {
                    val frameId = mapper.mapping(dc.document) ?: continue
                    frameDeleteDao.getSync(frameId)?.run {
                        deleteFile(context)
                    }
                    frameDeleteDao.delete(frameId)
                }

                setDate(config, CONFIG_RECENT_UPDATE_TIME)
            }
        }
        catch(e:Exception){
            crashlytics.log(e.message ?: "")
        }
    }

    companion object{
        const val COL_NAME = "theme_frames_delete"
        const val FIELD_UPDATE_TIME = "update_time"
        const val CONFIG_RECENT_UPDATE_TIME = "Config.ThemeFrameDelete.RecentUpdateTime"
    }
}