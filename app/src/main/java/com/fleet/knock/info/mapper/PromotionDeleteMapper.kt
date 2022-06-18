package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class PromotionDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[PROMOTION_ID] as String
    }

    companion object{
        private const val PROMOTION_ID = "promotion_id"
    }
}