package com.fleet.knock.info.theme

import android.content.Context
import androidx.room.*
import com.fleet.knock.info.user.User
import java.util.*

@Entity(tableName = "theme")
data class FThemeEntity(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "theme_id") val id:String,
                        @ColumnInfo(name = "theme_promotion_id") var promotionId:String,
                        @ColumnInfo(name = "theme_project_id") var projectId:String,
                        @Embedded val resource:FThemeResource,
                        @Embedded val info:FThemeInfo,
                        @ColumnInfo(name = "theme_allow_post") var allowPost:Boolean,
                        @ColumnInfo(name = "theme_allow_post_start_time") var allowPostStartTime:Date,
                        @ColumnInfo(name = "theme_apply_count", defaultValue = "0") var applyCount : Long,
                        @ColumnInfo(name = "theme_like_count", defaultValue = "0") var likeCount : Long,
                        @ColumnInfo(name = "theme_owner_uid") var ownerUid:String,
                        @ColumnInfo(name = "theme_update_time") var updateTime : Date,
                        @ColumnInfo(name = "theme_registered_time") val registeredTime : Date,
                        @ColumnInfo(name = "theme_post_start_time") var postStartTime : Date,
                        @ColumnInfo(name = "theme_post_end_time") var postEndTime : Date
) {
    var existThumbnail
        get() = resource.updateThumbnail
        set(value) {
            resource.updateThumbnail = value
        }

    var existPreload
        get() = resource.updatePreload
        set(value) {
            resource.updatePreload = value
        }

    var existTheme
        get() = resource.updateTheme
        set(value) {
            resource.updateTheme = value
        }

    fun getPreload(context: Context) = getPreloadProvider(context).image

    fun getPreloadProvider(context: Context) = resource.getPreload(context)

    fun getThumbnail(context: Context) = getThumbnailProvider(context).image

    fun getThumbnailProvider(context: Context) = resource.getThumbnail(context)

    fun getTheme(context: Context) = getThemeProvider(context).videoDataSpec

    fun getThemeSource(context: Context) = getThemeProvider(context).videoSource

    fun getThemeProvider(context: Context) = resource.getTheme(context)

    fun update(entity: FThemeEntity?): FThemeEntity {
        entity ?: return this

        resource.update(entity.resource)
        info.update(entity.info)

        promotionId = entity.promotionId
        projectId = entity.projectId
        allowPost = entity.allowPost
        allowPostStartTime = entity.allowPostStartTime
        applyCount = entity.applyCount
        likeCount = entity.likeCount
        ownerUid = entity.ownerUid
        updateTime = entity.updateTime
        postStartTime = entity.postStartTime
        postEndTime = entity.postEndTime

        return this
    }
}