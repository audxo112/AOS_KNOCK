package com.fleet.knock.info.promotion

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fleet.knock.info.provider.ImageProvider
import java.util.*


@Entity(tableName = "promotion")
data class FPromotion(@PrimaryKey(autoGenerate = false) @ColumnInfo(name="promotion_id") val id:String,
                      @ColumnInfo(name="promotion_title") var title:String,
                      @ColumnInfo(name="promotion_announce") var announce:String,
                      @ColumnInfo(name="promotion_event_link") var eventLink:String,
                      @ColumnInfo(name="promotion_banner_file_name") var bannerFileName:String,
                      @ColumnInfo(name="promotion_banner_ratio") var bannerRatio:String,
                      @ColumnInfo(name="promotion_enable_main_banner") var enableMainBanner:Boolean,
                      @ColumnInfo(name="promotion_update_banner") var updateBanner:Boolean,
                      @ColumnInfo(name="promotion_update_banner_time") var updateBannerTime:Date,
                      @ColumnInfo(name="promotion_main_file_name") var mainFileName:String,
                      @ColumnInfo(name="promotion_main_ratio") var mainRatio:String,
                      @ColumnInfo(name="promotion_update_main") var updateMain:Boolean,
                      @ColumnInfo(name="promotion_update_main_time") var updateMainTime:Date,
                      @ColumnInfo(name="promotion_priority") var priority:Long,
                      @ColumnInfo(name="promotion_update_time") var updateTime:Date,
                      @ColumnInfo(name="promotion_registered_time") val registeredTime:Date) {

    constructor(
        id: String,
        title: String,
        announce: String,
        eventLink: String,
        bannerExt: String,
        bannerRatio: String,
        updateBannerTime: Date,
        enableMainBanner: Boolean,
        mainExt: String,
        mainRatio: String,
        updateMainTime: Date,
        priority: Long,
        updateTime: Date,
        registeredTime: Date
    ) : this(
        id, title, announce, eventLink,
        "$id.$bannerExt",
        bannerRatio,
        enableMainBanner,
        false,
        updateBannerTime,
        "$id.$mainExt",
        mainRatio,
        false,
        updateMainTime,
        priority, updateTime, registeredTime
    )

    @Ignore
    private var _banner:ImageProvider? = null
    fun getBanner(context:Context) = getBannerProvider(context).image
    fun getBannerProvider(context: Context) = _banner ?: ImageProvider(context,
        DIR_PROMOTION,
        DIR_BANNER,
        bannerFileName).also{
        _banner = it
    }

    @Ignore
    private var _main:ImageProvider? = null
    fun getMain(context:Context) = getMainProvider(context).image
    fun getMainProvider(context: Context) = _main ?: ImageProvider(context,
        DIR_PROMOTION,
        DIR_MAIN,
        mainFileName).also{
        _main = it
    }

    fun update(promotion:FPromotion?) : FPromotion{
        promotion ?: return this
        title = promotion.title
        announce = promotion.announce
        eventLink = promotion.eventLink
        priority = promotion.priority
        updateTime = promotion.updateTime

        bannerFileName = promotion.bannerFileName
        bannerRatio = promotion.bannerRatio

        enableMainBanner = promotion.enableMainBanner

        if(updateBannerTime < promotion.updateBannerTime){
            updateBanner = false
            updateBannerTime = promotion.updateBannerTime
        }

        mainFileName = promotion.mainFileName
        mainRatio = promotion.mainRatio
        if(updateMainTime < promotion.updateMainTime){
            updateMain = promotion.updateMain
            updateMainTime = promotion.updateMainTime
        }

        return this
    }

    fun deleteFile(context:Context){
        getBannerProvider(context).deleteFile()
        getMainProvider(context).deleteFile()
    }

    companion object{
        @Ignore const val DIR_PROMOTION = "promotion"
        @Ignore const val DIR_BANNER = "banner"
        @Ignore const val DIR_MAIN = "main"
    }
}