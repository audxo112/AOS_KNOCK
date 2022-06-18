package com.fleet.knock.info.theme

import androidx.room.*
import java.util.*

@Entity(tableName = "theme_recommend")
//    , foreignKeys = [
//    ForeignKey(entity = FThemeEntity::class, parentColumns = ["theme_id"], childColumns = ["recommend_theme_id"])
//])
data class FThemeRecommend(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "recommend_id") val id:String,
                           @ColumnInfo(name = "recommend_theme_id") val themeId:String,
                           @ColumnInfo(name = "recommend_ready") val recommendReady : Boolean,
                           @ColumnInfo(name = "recommend_day") val recommendDay : Long,
                           @ColumnInfo(name = "recommend_start_time") val startTime : Date,
                           @ColumnInfo(name = "recommend_end_time") val endTime : Date
){
    constructor(id:String, themeId:String, recommendDay:Long, startTime:Date, endTime: Date) : this(id, themeId, true, recommendDay, startTime, endTime)

    companion object{
        @Ignore const val SUN = 1
        @Ignore const val MON = 2
        @Ignore const val TUE = 4
        @Ignore const val WED = 8
        @Ignore const val THU = 16
        @Ignore const val FRI = 32
        @Ignore const val SAT = 64
        @Ignore const val EVERYDAY = SUN or MON or TUE or WED or THU or FRI or SAT
    }
}