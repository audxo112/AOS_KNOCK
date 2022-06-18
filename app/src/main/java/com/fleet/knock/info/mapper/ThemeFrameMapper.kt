package com.fleet.knock.info.mapper

import com.fleet.knock.info.editor.ThemeFrame
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class ThemeFrameMapper :Mapper<ThemeFrame>{
    override fun mapping(s: QueryDocumentSnapshot): ThemeFrame? {
        return ThemeFrame(s.id,
            s[TITLE] as String,
            s[TYPE] as String,
            s[PRIORITY] as Long,
            s[SCALE_TYPE] as String,
            if(s.contains(REPEAT_MODE)) s[REPEAT_MODE] as String
            else ThemeFrame.REPEAT_MODE_NONE,
            HashSet(s[OFFERING_SCREEN] as ArrayList<String>),
            s[MINI_THUMBNAIL_EXT] as String,
            false,
            s[THUMBNAIL_EXT] as String,
            false,
            s[FRAME_EXT] as String,
            false,
            (s[UPDATE_TIME] as Timestamp).toDate(),
            Date(0L),
            Date(0L),
            (s[REGISTERED_TIME] as Timestamp).toDate()
        )
    }

    companion object{
        private const val TITLE= "title"
        private const val TYPE = "type"
        private const val PRIORITY = "priority"
        private const val SCALE_TYPE = "scale_type"
        private const val MINI_THUMBNAIL_EXT = "mini_thumbnail_ext"
        private const val THUMBNAIL_EXT = "thumbnail_ext"
        private const val FRAME_EXT = "frame_ext"
        private const val REPEAT_MODE = "repeat_mode"
        private const val OFFERING_SCREEN = "offering_screen"
        private const val UPDATE_TIME = "update_time"
        private const val REGISTERED_TIME = "registered_time"
    }
}