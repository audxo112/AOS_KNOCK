package com.fleet.knock.info.theme

import androidx.room.ColumnInfo
import androidx.room.Ignore
import kotlin.collections.HashSet

data class FThemeInfo(@ColumnInfo(name = "theme_info_title") var title:String,
                      @ColumnInfo(name = "theme_info_content") var content:String,
                      @ColumnInfo(name = "theme_info_hash_tag") var hashTag:HashSet<String>,
                      @ColumnInfo(name = "theme_info_link") var link:String,
                      @ColumnInfo(name = "theme_info_provide_type") var provideType : String,
                      @ColumnInfo(name = "theme_info_price") var price : Long,
                      @ColumnInfo(name = "theme_info_provision_day") var provisionDay:Long
){
    fun update(info: FThemeInfo){
        title = info.title
        content = info.content
        hashTag = info.hashTag
        link = info.link
        provideType = info.provideType
        price = info.price
        provisionDay = info.provisionDay
    }

    companion object{
        @Ignore const val PROVIDE_TYPE_FREE = "Free"
        @Ignore const val PROVIDE_TYPE_PAY = "Pay"
        @Ignore const val PROVIDE_TYPE_EVENT = "Event"
    }
}