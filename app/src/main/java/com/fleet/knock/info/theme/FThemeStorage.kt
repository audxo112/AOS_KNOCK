package com.fleet.knock.info.theme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "theme_storage")
data class FThemeStorage (@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "storage_id") val id:Long?,
                          @ColumnInfo(name = "storage_theme_id") val themeId:String,
                          @ColumnInfo(name = "storage_user_uid") val userUid:String,
                          @ColumnInfo(name = "storage_registered_time") val registeredTime:Date,
                          @ColumnInfo(name = "storage_recent_apply_time")val recentApplyTime:Date) {

    constructor(
        themeId: String,
        userUid: String,
        registeredTime: Date,
        recentApplyTime: Date
    ) : this(null, themeId, userUid, registeredTime, recentApplyTime)
}