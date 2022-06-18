package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class ThemeDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[THEME_ID] as String
    }

    companion object{
        private const val THEME_ID = "theme_id"
    }
}