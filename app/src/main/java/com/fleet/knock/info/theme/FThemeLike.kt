package com.fleet.knock.info.theme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "theme_like")
data class FThemeLike(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "like_id") val id:Long?,
                      @ColumnInfo(name="like_theme_id") val themeId:String,
                      @ColumnInfo(name="like_user_uid") val userUid:String,
                      @ColumnInfo(name="liked_time") val likedTime: Date){

    constructor(themeId:String, userUid:String, likedTime:Date) : this(null, themeId, userUid, likedTime)
}