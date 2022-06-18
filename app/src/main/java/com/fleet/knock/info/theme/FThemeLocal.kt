package com.fleet.knock.info.theme

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fleet.knock.info.provider.ImageProvider
import com.fleet.knock.info.provider.VideoProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import java.util.*

@Entity(tableName = "theme_local")
data class FThemeLocal (@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "local_id") val id :Long?,
                        @Embedded(prefix = "local_") val resource:FThemeResource,
                        @ColumnInfo(name = "local_owner_uid") val ownerUid:String,
                        @ColumnInfo(name = "local_shared") val localShared:Boolean,
                        @ColumnInfo(name = "local_saved_file_name") var savedFileName:String,
                        @ColumnInfo(name = "local_recent_apply_time") val recentApplyTime:Date,
                        @ColumnInfo(name = "local_registered_time") val registeredTime: Date){

    constructor(ownerUid:String, registeredTime: Date) : this(null,
        FThemeResource(registeredTime),
        ownerUid,
        false,
        "",
        registeredTime,
        registeredTime)

    fun getThumbnail(context : Context) = getThumbnailProvider(context).image

    fun getThumbnailProvider(context : Context) = resource.getThumbnail(context)

    fun getPreload(context : Context) = getPreloadProvider(context).image

    fun getPreloadProvider(context : Context) = resource.getPreload(context)

    fun getTheme(context : Context) = getThemeProvider(context).videoDataSpec

    fun getThemeSource(context : Context) = getThemeProvider(context).videoSource

    fun getThemeProvider(context: Context) = resource.getTheme(context)

    fun deleteFile(context:Context){
        resource.deleteFile(context)
    }
}