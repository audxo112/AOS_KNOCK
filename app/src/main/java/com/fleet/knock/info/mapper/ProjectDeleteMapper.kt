package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class ProjectDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[PROJECT_ID] as String
    }

    companion object{
        private const val PROJECT_ID = "project_id"
    }
}