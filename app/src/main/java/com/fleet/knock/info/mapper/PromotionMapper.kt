package com.fleet.knock.info.mapper

import com.fleet.knock.info.promotion.FPromotion
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class PromotionMapper : Mapper<FPromotion>{
    override fun mapping(s: QueryDocumentSnapshot): FPromotion? {
        return FPromotion(
            s.id,
            s[TITLE] as String,
            s[ANNOUNCE] as String,
            s[EVENT_LINK] as String,
            s[BANNER_EXT] as String,
            if(s.contains(BANNER_RATIO)) s[BANNER_RATIO] as String else "",
            (s[UPDATE_BANNER_TIME] as Timestamp).toDate(),
            if(s.contains(ENABLE_MAIN_BANNER)) s[ENABLE_MAIN_BANNER] as Boolean else true,
            s[MAIN_EXT] as String,
            if(s.contains(MAIN_RATIO)) s[MAIN_RATIO] as String else "",
            (s[UPDATE_MAIN_TIME] as Timestamp).toDate(),
            s[PRIORITY] as Long,
            (s[UPDATE_TIME] as Timestamp).toDate(),
            (s[REGISTERED_TIME] as Timestamp).toDate()
        )
    }

    companion object{
        private const val TITLE = "title"
        private const val ANNOUNCE = "announce"
        private const val EVENT_LINK = "event_link"
        private const val BANNER_EXT = "banner_ext"
        private const val BANNER_RATIO = "banner_ratio"
        private const val UPDATE_BANNER_TIME = "update_banner_time"
        private const val ENABLE_MAIN_BANNER = "enable_main_banner"
        private const val MAIN_EXT = "main_ext"
        private const val MAIN_RATIO = "main_ratio"
        private const val UPDATE_MAIN_TIME = "update_main_time"
        private const val PRIORITY = "priority"
        private const val UPDATE_TIME = "update_time"
        private const val REGISTERED_TIME = "registered_time"
    }
}