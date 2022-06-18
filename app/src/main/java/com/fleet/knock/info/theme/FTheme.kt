package com.fleet.knock.info.theme

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.room.Embedded
import com.fleet.knock.info.provider.ImageProvider
import com.fleet.knock.info.provider.VideoProvider
import com.fleet.knock.info.user.User
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec

data class FTheme(@Embedded val theme:FThemeEntity,
                  @Embedded val user: User){
    val id
        get() = theme.id
    val promotionId
        get() = theme.promotionId
    val projectId
        get() = theme.projectId
    var existPreload
        get() = theme.resource.updatePreload
        set(value){
            theme.resource.updatePreload = value
        }
    var existThumbnail
        get() = theme.resource.updateThumbnail
        set(value){
            theme.resource.updateThumbnail = value
        }
    var existTheme
        get() = theme.resource.updateTheme
        set(value){
            theme.resource.updateTheme = value
        }
    var updateUserAvatar
        get() = user.updateAvatar
        set(value){
            user.updateAvatar = value
        }

    val themeCopyright
        get() = "${user.nickname} 님의 ${theme.info.title} 테마"

    val themeTitle
        get() = theme.info.title

    val themeContent
        get() = theme.info.content

    val themeHashTag
        get() = theme.info.hashTag

    val themeLink
        get() = theme.info.link

    val userUid
        get() = user.userUid

    val userId
        get() = user.userId

    val userNickname
        get() = user.nickname

    val userGrade
        get() = user.grade

    fun getUserAvatar(context: Context) = user.getAvatar(context).image

    fun getUserAvatarProvider(context: Context) = user.getAvatar(context)

    fun getPreload(context: Context) = getPreloadProvider(context).image

    fun getPreloadProvider(context:Context) = theme.resource.getPreload(context)

    fun getThumbnail(context : Context) = getThumbnailProvider(context).image

    fun getThumbnailProvider(context:Context) = theme.resource.getThumbnail(context)

    fun getTheme(context : Context) = getThemeProvider(context).videoDataSpec

    fun getThemeSource(context : Context) = getThemeProvider(context).videoSource

    fun getThemeProvider(context:Context) = theme.resource.getTheme(context)

    fun updateThemeEntity(entity:FThemeEntity) : FThemeEntity{
        return theme.update(entity)
    }

    fun deleteFile(context:Context){
        theme.resource.deleteFile(context)
    }
}