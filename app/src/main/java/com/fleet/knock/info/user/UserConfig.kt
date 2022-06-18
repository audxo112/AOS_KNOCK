package com.fleet.knock.info.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfig(@PrimaryKey(autoGenerate = false) @ColumnInfo(name ="config_user_uid")val userUid:String,
                      @ColumnInfo(name = "config_selected_theme_type") var selectedThemeType:String,
                      @ColumnInfo(name = "config_selected_public_theme_id") var selectedPublicThemeId:String,
                      @ColumnInfo(name = "config_selected_local_theme_id") var selectedLocalThemeId:Long){

    constructor(userUid:String) : this(userUid, "", "", -1L)

    val isValidPublicTheme:Boolean
        get() = selectedThemeType == SELECTED_THEME_TYPE_PUBLIC && selectedPublicThemeId != ""

    val isValidLocalTheme:Boolean
        get() = selectedThemeType == SELECTED_THEME_TYPE_LOCAL && selectedLocalThemeId != -1L

    val existSelectedTheme:Boolean
        get() = when(selectedThemeType){
            SELECTED_THEME_TYPE_PUBLIC -> selectedPublicThemeId != ""
            SELECTED_THEME_TYPE_LOCAL -> selectedLocalThemeId != -1L
            else -> false
        }

    fun isSelectedTheme(publicId:String) : Boolean{
        return if(selectedThemeType == SELECTED_THEME_TYPE_PUBLIC){
            selectedPublicThemeId == publicId
        }else{
            false
        }
    }

    fun selectTheme(publicId:String?){
        publicId ?: return

        selectedThemeType = SELECTED_THEME_TYPE_PUBLIC
        selectedPublicThemeId = publicId
    }

    fun isSelectedTheme(localId:Long) : Boolean{
        return if(selectedThemeType == SELECTED_THEME_TYPE_LOCAL){
            selectedLocalThemeId == localId
        }else{
            false
        }
    }

    fun selectTheme(localId:Long?){
        localId ?: return

        selectedThemeType = SELECTED_THEME_TYPE_LOCAL
        selectedLocalThemeId = localId
    }

    companion object{
        const val SELECTED_THEME_TYPE_PUBLIC = "SelectedThemeType.Public"
        const val SELECTED_THEME_TYPE_LOCAL = "SelectedThemeType.Local"
    }
}