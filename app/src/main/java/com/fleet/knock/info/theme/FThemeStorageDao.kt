package com.fleet.knock.info.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface FThemeStorageDao {
    @Query("""SELECT t.*, u.*
        FROM theme_storage s
        JOIN theme t ON s.storage_theme_id = t.theme_id
        JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE s.storage_user_uid = :userUid
        ORDER BY s.storage_recent_apply_time DESC""")
    fun getAll(userUid:String) : List<FTheme>

    @Query("""SELECT storage_id 
        FROM theme_storage 
        WHERE storage_user_uid = :userUid AND
        storage_theme_id = :themeId""")
    fun exist(userUid:String, themeId:String) : Long?

    @Insert
    fun insert(storage:FThemeStorage)

    @Query("""UPDATE theme_storage
        SET storage_recent_apply_time = :recentApplyTime
        WHERE storage_user_uid = :userUid AND
        storage_theme_id = :themeId""")
    fun updateRecentApplyTime(userUid:String, themeId:String, recentApplyTime: Date)

    @Query("DELETE FROM theme_storage WHERE storage_theme_id = :themeId")
    fun delete(themeId:String)
}