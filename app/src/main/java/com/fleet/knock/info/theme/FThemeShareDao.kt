package com.fleet.knock.info.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FThemeShareDao{
    @Query("""SELECT *
        FROM theme_share s
        JOIN theme t ON s.share_theme_id = t.theme_id
        JOIN user u ON :userUid = u.user_uid
        ORDER BY s.share_time DESC""")
    fun getAll(userUid:String) : List<FTheme>

    @Query("""SELECT SUM(t.theme_apply_count)
        FROM theme_share s
        JOIN theme t ON s.share_theme_id = t.theme_id
        WHERE t.theme_owner_uid = :userUid
    """)
    fun getApplyTotal(userUid:String) : Long

    @Query("""SELECT SUM(t.theme_like_count)
        FROM theme_share s
        JOIN theme t ON s.share_theme_id = t.theme_id
        WHERE t.theme_owner_uid = :userUid
    """)
    fun getLikeTotal(userUid:String) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(share:FThemeShare)

    @Query("""UPDATE theme_share 
        SET share_confirm = :confirm
        WHERE share_id = :shareId""")
    fun updateShareState(shareId:String, confirm:Boolean)

    @Query("DELETE FROM theme_share WHERE share_theme_id = :themeId")
    fun delete(themeId:String)
}