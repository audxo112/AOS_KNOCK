package com.fleet.knock.info.theme

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FThemeLikeDao{
    @Query("""SELECT t.*, u.*
        FROM theme_like l
        JOIN theme t ON l.like_theme_id = t.theme_id
        JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE u.user_uid = :userUid
        ORDER BY l.liked_time DESC""")
    fun getAll(userUid:String) : LiveData<List<FTheme>>

    @Query("""SELECT * 
        FROM theme_Like 
        WHERE like_user_uid = :userUid AND
        like_theme_id = :themeId""")
    fun get(userUid:String, themeId:String) : FThemeLike?

    @Insert
    fun insert(like:FThemeLike)

    @Query("DELETE FROM theme_like WHERE like_theme_id = :themeId")
    fun delete(themeId:String)

}