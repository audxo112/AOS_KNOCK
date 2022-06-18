package com.fleet.knock.info.mapper

import com.fleet.knock.info.tag.Tag
import com.google.firebase.firestore.QueryDocumentSnapshot

class TagMapper : Mapper<Tag>{
    override fun mapping(s: QueryDocumentSnapshot): Tag? {
        return Tag(s.id,
            s[NAME] as String,
            s[COUNT] as Long)
    }

    companion object {
        private const val NAME = "name"
        private const val COUNT = "count"
    }
}