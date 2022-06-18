package com.fleet.knock.info.mapper

import com.fleet.knock.info.theme.FThemeRecommend
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class ThemeRecommendMapper : Mapper<FThemeRecommend>{
    override fun mapping(s: QueryDocumentSnapshot): FThemeRecommend?{
        return FThemeRecommend(s.id,
            s[THEME_ID] as String,
            s[RECOMMEND_DAY] as Long,
            (s[START_TIME] as Timestamp).toDate(),
            (s[END_TIME] as Timestamp).toDate())
    }

    companion object {
        private const val THEME_ID = "theme_id"
        private const val RECOMMEND_DAY = "recommend_day"
        private const val START_TIME = "start_time"
        private const val END_TIME = "end_time"
    }
}