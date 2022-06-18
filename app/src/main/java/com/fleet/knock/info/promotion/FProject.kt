package com.fleet.knock.info.promotion

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.fleet.knock.info.theme.FThemeEntity
import java.util.*
import kotlin.collections.ArrayList

data class FProject(
    @Embedded val project:FProjectEntity
){
    @Ignore
    val themes = ArrayList<FThemeEntity>()

    val projectId
        get() = project.id
    val promotionId
        get() = project.promotionId
    val title
        get() = project.title
}
