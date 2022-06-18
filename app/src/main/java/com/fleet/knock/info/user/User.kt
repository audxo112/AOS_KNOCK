package com.fleet.knock.info.user

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fleet.knock.info.provider.ImageProvider
import java.util.*

@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "user_uid") val userUid:String,
                @ColumnInfo(name = "user_id") var userId : String,
                @ColumnInfo(name = "user_nickname") var nickname : String,
                @ColumnInfo(name = "user_introduce") var introduce : String,
                @ColumnInfo(name = "user_grade") var grade : String,
                @ColumnInfo(name = "user_exist_avatar") var existAvatar : Boolean,
                @ColumnInfo(name = "user_avatar_ext") var avatarExt : String,
                @ColumnInfo(name = "user_update_avatar") var updateAvatar : Boolean,
                @ColumnInfo(name = "user_update_avatar_time") var updateAvatarTime : Date,
                @ColumnInfo(name = "user_upload_stop_period") var uploadStopPeriod : Date,
                @ColumnInfo(name = "user_update_time") var updateTime : Date){

    private val avatarFileName:String
        get() = "${userUid}.${avatarExt}"

    @Ignore private var _avatar: ImageProvider? = null
    fun getAvatar(context: Context) = _avatar ?: ImageProvider(context,
        DIR_USERS,
        DIR_AVATAR,
        avatarFileName).also {
        _avatar = it
    }

    fun update(user:User?) : User {
        user ?: return this
        userId = user.userId
        nickname = user.nickname
        introduce = user.introduce
        grade = user.grade
        existAvatar = user.existAvatar
        avatarExt = user.avatarExt
        uploadStopPeriod = user.uploadStopPeriod
        updateTime = user.updateTime

        if(updateAvatarTime < user.updateAvatarTime){
            updateAvatar = false
            updateAvatarTime = user.updateAvatarTime
        }

        return this
    }

    fun deleteFile(context:Context){
        getAvatar(context).deleteFile()
    }

    companion object{
        @Ignore const val DEFAULT_UID = "Default"

        @Ignore const val DIR_USERS = "users"
        @Ignore const val DIR_AVATAR = "avatar"

        @Ignore const val GRADE_NORMAL = "normal"
        @Ignore const val GRADE_BUSINESS = "business"
        @Ignore const val GRADE_ARTIST = "artist"
    }
}