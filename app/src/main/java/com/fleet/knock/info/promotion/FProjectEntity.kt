package com.fleet.knock.info.promotion

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName ="promotion_project")
data class FProjectEntity(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "project_id") val id:String,
                          @ColumnInfo(name = "project_promotion_id") var promotionId:String,
                          @ColumnInfo(name = "project_title")var title:String,
                          @ColumnInfo(name = "project_priority") var priority:Long,
                          @ColumnInfo(name = "project_update_time") var updateTime: Date,
                          @ColumnInfo(name = "project_registered_time") val registeredTime:Date){

    fun update(entity:FProjectEntity?) : FProjectEntity{
        entity ?: return this
        promotionId = entity.promotionId
        title = entity.title
        priority = entity.priority
        updateTime = entity.updateTime

        return this
    }
}