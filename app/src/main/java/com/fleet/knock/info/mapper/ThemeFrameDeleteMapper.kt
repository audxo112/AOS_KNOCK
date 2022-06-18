package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class ThemeFrameDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[FRAME_ID] as String
    }

    companion object{
        private const val FRAME_ID = "frame_id"
    }
}