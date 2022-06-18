package com.fleet.knock.utils.dataloader
import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.UserMapper
import com.fleet.knock.info.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FDLUser() : FDLoader<User>(COL_NAME, UserMapper()) {
    override suspend fun load(
        context: Context,
        fdb: FirebaseFirestore,
        db: KNOCKDatabase,
        config: SharedPreferences
    ) {
        try {
            val query = fdb.collection(name)
                .whereEqualTo(FIELD_UPLOADER, true)
                .whereGreaterThan(FIELD_UPDATE_TIME, getDate(config, CONFIG_RECENT_UPDATE_TIME))
                .get()
                .await()

            if(!query.isEmpty) {
                for (dc in query.documentChanges) {
                    val t = mapper.mapping(dc.document) ?: continue
                    db.userDao().replace(t)
                }

                setDate(config, CONFIG_RECENT_UPDATE_TIME)
            }
        }
        catch(e:Exception){
            crashlytics.log(e.message ?: "")
        }
    }

    companion object {
        const val COL_NAME = "users"

        const val FIELD_UPLOADER = "uploader"
        const val FIELD_UPDATE_TIME = "update_time"

        const val CONFIG_RECENT_UPDATE_TIME = "Config.User.RecentUpdateTime"
    }
}