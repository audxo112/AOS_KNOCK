package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

class UserDeleteMapper : Mapper<String>{
    override fun mapping(s: QueryDocumentSnapshot): String? {
        return s[USER_UID] as String
    }

    companion object{
        private const val USER_UID = "user_uid"
    }
}