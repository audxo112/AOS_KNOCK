package com.fleet.knock.info.theme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "theme_share")
data class FThemeShare(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "share_id") val shareId:String,
                       @ColumnInfo(name = "share_theme_id") val themeId:String,
                       @ColumnInfo(name = "share_state") val shareState:String,
                       @ColumnInfo(name = "share_confirm", defaultValue = "0") val confirm:Boolean,
                       @ColumnInfo(name = "share_deny_type") val denyType:String,
                       @ColumnInfo(name = "share_deny_reason") val denyReason:String,
                       @ColumnInfo(name = "share_time") val shareTime:Date,
                       @ColumnInfo(name = "share_update_time") val updateTime: Date) {
}