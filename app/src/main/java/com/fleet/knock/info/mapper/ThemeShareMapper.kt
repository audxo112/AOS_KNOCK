package com.fleet.knock.info.mapper

import com.fleet.knock.info.theme.FThemeShare
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class ThemeShareMapper : Mapper<FThemeShare>{
    override fun mapping(s: QueryDocumentSnapshot): FThemeShare? {
        return FThemeShare(s.id,
            s[THEME_ID] as String,
            s[SHARE_STATE] as String,
            false,
            s[DENY_TYPE] as String,
            s[DENY_REASON] as String,
            (s[SHARE_TIME] as Timestamp).toDate(),
            (s[UPDATE_TIME] as Timestamp).toDate())


    }

    companion object{
        private const val THEME_ID = "theme_id"
        private const val SHARE_STATE = "share_state"
        private const val DENY_TYPE = "deny_type"
        private const val DENY_REASON = "deny_reason"
        private const val SHARE_TIME = "share_time"
        private const val UPDATE_TIME = "update_time"
    }
}