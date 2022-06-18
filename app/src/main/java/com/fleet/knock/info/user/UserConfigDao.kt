package com.fleet.knock.info.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserConfigDao {
    @Query("SELECT * FROM user_config WHERE config_user_uid = :userUid")
    fun getLive(userUid: String) : LiveData<UserConfig?>

    @Query("SELECT * FROM user_config WHERE config_user_uid = :userUid")
    suspend fun getSync(userUid: String) : UserConfig?

    @Insert
    suspend fun insert(userConfig:UserConfig)

    @Query("""UPDATE user_config 
        SET config_selected_theme_type = '${UserConfig.SELECTED_THEME_TYPE_PUBLIC}',
        config_selected_public_theme_id = :themeId
        WHERE config_user_uid = :userUid""")
    suspend fun selectPublicTheme(userUid:String, themeId:String)

    @Query("""UPDATE user_config 
        SET config_selected_theme_type = '${UserConfig.SELECTED_THEME_TYPE_LOCAL}',
        config_selected_local_theme_id = :themeId
        WHERE config_user_uid = :userUid""")
    suspend fun selectLocalTheme(userUid:String, themeId:Long)

    @Query("""UPDATE user_config
        SET config_selected_theme_type = "",
        config_selected_public_theme_id = "",
        config_selected_local_theme_id = -1
        WHERE config_user_uid = :userUid""")
    suspend fun removeSelectTheme(userUid:String)
}