package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class ThemeRecommendsDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[RECOMMEND_ID] as String
    }

    companion object{
        private const val RECOMMEND_ID = "recommend_id"
    }
}