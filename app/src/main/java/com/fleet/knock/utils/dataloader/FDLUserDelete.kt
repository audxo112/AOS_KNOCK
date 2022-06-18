package com.fleet.knock.utils.dataloader
import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.UserDeleteMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FDLUserDelete () : FDLoader<String>(COL_NAME,
    UserDeleteMapper()){

    override suspend fun load(
        context:Context,
        fdb: FirebaseFirestore,
        db: KNOCKDatabase,
        config: SharedPreferences
    ) {
        try {
            val query = fdb.collection(name)
                .whereGreaterThan(FIELD_UPDATE_TIME, getDate(config, CONFIG_RECENT_UPDATE_TIME))
                .get()
                .await()

            if(!query.isEmpty) {
                val userDao = db.userDao()

                for (dc in query.documentChanges) {
                    val userUid = mapper.mapping(dc.document) ?: continue

                    userDao.getSync(userUid)?.run {
                        deleteFile(context)
                    }
                    userDao.delete(userUid)
                }

                setDate(config, CONFIG_RECENT_UPDATE_TIME)
            }
        }
        catch (e:Exception){
            crashlytics.log(e.message ?: "")
        }
    }

    companion object{
        const val COL_NAME = "users_delete"

        const val FIELD_UPDATE_TIME = "update_time"

        const val CONFIG_RECENT_UPDATE_TIME = "Config.UserDelete.RecentUpdateTime"
    }
}