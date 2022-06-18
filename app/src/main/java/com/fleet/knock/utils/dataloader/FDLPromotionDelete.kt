package com.fleet.knock.utils.dataloader
import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.PromotionDeleteMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FDLPromotionDelete() :FDLoader<String>(COL_NAME, PromotionDeleteMapper()){

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

            if(!query.isEmpty) {
                val promotionDao = db.promotionDao()

                for (dc in query.documentChanges) {
                    val promotionId = mapper.mapping(dc.document) ?: continue

                    promotionDao.getSync(promotionId)?.run {
                        deleteFile(context)
                    }
                    promotionDao.delete(promotionId)
                }

                setDate(config, CONFIG_RECENT_UPDATE_TIME)
            }
        }
        catch (e:Exception){
            crashlytics.log(e.message ?: "")
        }
    }

    companion object{
        const val COL_NAME = "promotions_delete"

        const val FIELD_UPDATE_TIME = "update_time"

        const val CONFIG_RECENT_UPDATE_TIME = "Config.PromotionDelete.RecentUpdateTime"
    }
}