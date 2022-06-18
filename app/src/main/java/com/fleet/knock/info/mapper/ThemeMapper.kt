package com.fleet.knock.info.mapper

import com.fleet.knock.info.theme.FThemeEntity
import com.fleet.knock.info.theme.FThemeInfo
import com.fleet.knock.info.theme.FThemeResource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ThemeMapper : Mapper<FThemeEntity>{
    override fun mapping(s: QueryDocumentSnapshot): FThemeEntity? {
        val r: HashMap<String, Any?> = s[RESOURCE] as HashMap<String, Any?>
        val i = s[INFO] as HashMap<String, Any?>
        return FThemeEntity(s.id,
            s[PROMOTION_ID] as String,
            s[PROJECT_ID] as String,
            FThemeResource(s.id,
                r[RESOURCE_THUMBNAIL_EXT] as String,
                (r[RESOURCE_UPDATE_THUMBNAIL_TIME] as Timestamp).toDate(),
                r[RESOURCE_PRELOAD_EXT] as String,
                r[RESOURCE_THEME_EXT] as String,
                HashSet(r[RESOURCE_OFFERING_SCREEN] as ArrayList<String>)),
            FThemeInfo(i[INFO_TITLE] as String,
                i[INFO_CONTENT] as String,
                HashSet(i[INFO_HASH_TAG] as ArrayList<String>),
                if(i.containsKey(INFO_LINK)) i[INFO_LINK] as String else "",
                i[INFO_PROVIDE_TYPE] as String,
                i[INFO_PRICE] as Long,
                i[INFO_PROVISION_DAY] as Long),
            s[ALLOW_POST] as Boolean,
            (s[ALLOW_POST_START_TIME] as Timestamp).toDate(),
            s[APPLY_COUNT] as Long,
            s[LIKE_COUNT] as Long,
            s[OWNER_UID] as String,
            (s[UPDATE_TIME] as Timestamp).toDate(),
            (s[REGISTERED_TIME] as Timestamp).toDate(),
            (s[POST_START_TIME] as Timestamp).toDate(),
            (s[POST_END_TIME] as Timestamp).toDate()
        )
    }

    companion object {
        private const val RESOURCE = "resource"
        private const val RESOURCE_THUMBNAIL_EXT = "thumbnail_ext"
        private const val RESOURCE_UPDATE_THUMBNAIL_TIME = "update_thumbnail_time"
        private const val RESOURCE_PRELOAD_EXT = "preload_ext"
        private const val RESOURCE_THEME_EXT = "theme_ext"
        private const val RESOURCE_OFFERING_SCREEN = "offering_screen"

        private const val INFO = "info"
        private const val INFO_TITLE = "title"
        private const val INFO_CONTENT = "content"
        private const val INFO_HASH_TAG = "hash_tag"
        private const val INFO_LINK = "link"
        private const val INFO_PROVIDE_TYPE = "provide_type"
        private const val INFO_PRICE = "price"
        private const val INFO_PROVISION_DAY = "provision_day"

        private const val PROMOTION_ID = "promotion_id"
        private const val PROJECT_ID = "project_id"
        private const val ALLOW_POST = "allow_post"
        private const val ALLOW_POST_START_TIME = "allow_post_start_time"
        private const val APPLY_COUNT = "apply_count"
        private const val LIKE_COUNT = "like_count"
        private const val OWNER_UID = "owner_uid"
        private const val UPDATE_TIME = "update_time"
        private const val REGISTERED_TIME = "registered_time"
        private const val POST_START_TIME = "post_start_time"
        private const val POST_END_TIME = "post_end_time"
    }
}