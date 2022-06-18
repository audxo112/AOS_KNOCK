package com.fleet.knock.utils.dataloader
import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.ProjectMapper
import com.fleet.knock.info.promotion.FProjectEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FDLProject() : FDLoader<FProjectEntity>(COL_NAME, ProjectMapper()){

    override suspend fun load(
        context: Context,
        fdb: FirebaseFirestore,
        db: KNOCKDatabase,
        config: SharedPreferences
    ) {
        try {
            val query = fdb.collection(name)
                .whereGreaterThan(FIELD_UPDATE_TIME, getDate(config, CONFIG_RECENT_UPDATE_TIME))
                .get()
                .await()

            if (!query.isEmpty) {
                for (dc in query.documentChanges) {
                    val p = mapper.mapping(dc.document) ?: continue
                    db.projectDao().replace(p)
                }

                setDate(config, CONFIG_RECENT_UPDATE_TIME)
            }
        }
        catch (e:Exception){
            crashlytics.log(e.message ?: "")
        }
    }

    companion object{
        const val COL_NAME = "projects"

        const val FIELD_UPDATE_TIME = "update_time"

        const val CONFIG_RECENT_UPDATE_TIME = "Config.Project.RecentUpdateTime"
    }
}