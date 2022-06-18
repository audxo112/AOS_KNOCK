package com.fleet.knock.info.mapper

import com.fleet.knock.info.promotion.FProjectEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class ProjectMapper : Mapper<FProjectEntity>{
    override fun mapping(s: QueryDocumentSnapshot): FProjectEntity? {
        return FProjectEntity(s.id,
            s[PROMOTION_ID] as String,
            s[TITLE] as String,
            s[PRIORITY] as Long,
            (s[UPDATE_TIME] as Timestamp).toDate(),
            (s[REGISTERED_TIME] as Timestamp).toDate()
        )
    }

    companion object{
        const val PROMOTION_ID = "promotion_id"
        const val TITLE = "title"
        const val PRIORITY = "priority"
        const val UPDATE_TIME = "update_time"
        const val REGISTERED_TIME = "registered_time"
    }
}