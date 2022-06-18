package com.fleet.knock.info.mapper

import com.fleet.knock.info.user.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class UserMapper : Mapper<User> {
    override fun mapping(s: QueryDocumentSnapshot): User? {
        return User(
            s.id,
            s[USER_ID] as String,
            s[NICKNAME] as String,
            s[INTRODUCE] as String,
            s[GRADE] as String,
            s[EXIST_AVATAR] as Boolean,
            s[AVATAR_EXT] as String,
            false,
            (s[UPDATE_AVATAR_TIME] as Timestamp).toDate(),
            (s[UPLOAD_STOP_PERIOD] as Timestamp).toDate(),
            (s[UPDATE_TIME] as Timestamp).toDate()
        )
    }

    companion object {
        private const val USER_ID = "user_id"
        private const val NICKNAME = "nickname"
        private const val INTRODUCE = "introduce"
        private const val GRADE = "grade"
        private const val EXIST_AVATAR = "exist_avatar"
        private const val AVATAR_EXT = "avatar_ext"
        private const val UPDATE_AVATAR_TIME = "update_avatar_time"
        private const val UPLOAD_STOP_PERIOD = "upload_stop_period"
        private const val UPDATE_TIME = "update_time"
    }
}