package com.fleet.knock.info.theme

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface FThemeDao{
    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_allow_post = 1 AND 
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime AND
        t.theme_registered_time >= :todayTime - 604800000
        ORDER BY t.theme_like_count DESC, 
            t.theme_registered_time ASC""")
    fun getRecentAll(todayTime: Date) : LiveData<List<FTheme>>

    @Query("""SELECT COUNT(*)
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_allow_post = 1 AND 
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime AND
        t.theme_registered_time >= :todayTime - 604800000
        ORDER BY t.theme_like_count DESC, 
            t.theme_registered_time ASC""")
    fun getRecentAllCount(todayTime: Date) : LiveData<Int>

    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_promotion_id = "" AND
        t.theme_project_id = "" AND
        t.theme_allow_post = 1 AND 
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime
        ORDER BY t.theme_like_count DESC, t.theme_registered_time DESC""")
    fun getAll(todayTime: Date) : LiveData<List<FTheme>>

    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_project_id = :projectId AND
        t.theme_allow_post = 1 AND
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime
        ORDER BY t.theme_like_count DESC, t.theme_registered_time DESC""")
    fun getAllInProject(projectId:String, todayTime:Date) : LiveData<List<FTheme>>

    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_allow_post = 1 AND 
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime AND
        t.theme_registered_time >= :todayTime - 604800000
        ORDER BY t.theme_like_count DESC, 
            t.theme_registered_time ASC""")
    fun getRecentAllSync(todayTime: Date) : List<FTheme>

    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_promotion_id = "" AND
        t.theme_project_id = "" AND
        t.theme_allow_post = 1 AND 
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime
        ORDER BY t.theme_like_count DESC, t.theme_registered_time DESC""")
    suspend fun getAllSync(todayTime: Date) : List<FTheme>

    @Query("""SELECT *
        FROM theme t
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_project_id = :projectId AND
        t.theme_allow_post = 1 AND
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime
        ORDER BY t.theme_like_count DESC, t.theme_registered_time DESC""")
    suspend fun getAllInProjectSync(projectId:String, todayTime:Date) : List<FTheme>

    @Query("""SELECT t.*, u.*
        FROM theme t
        INNER JOIN promotion_project p on p.project_id = t.theme_project_id
        INNER JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE
        t.theme_promotion_id = :promotionId AND
        t.theme_allow_post = 1 AND
        t.theme_post_start_time <= :todayTime AND
        t.theme_post_end_time >= :todayTime
        ORDER BY project_priority DESC, 
            project_registered_time DESC, 
            t.theme_like_count DESC, 
            t.theme_registered_time DESC""")
    suspend fun getAllInPromotionSync(promotionId:String, todayTime:Date) : List<FTheme>

    @Query("""SELECT *
        FROM theme
        INNER JOIN user ON theme_owner_uid = user.user_uid
        WHERE theme_id = :id """)
    suspend fun getSync(id:String) : FTheme?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(theme:FThemeEntity)

    @Transaction
    suspend fun replace(theme:FThemeEntity){
        val t = getSync(theme.id)
        if(t == null){
            insert(theme)
        }
        else{
            update(t.updateThemeEntity(theme))
        }
    }

    @Update
    suspend fun update(theme:FThemeEntity)

    @Query("""UPDATE theme SET theme_resource_update_thumbnail = 1 WHERE theme_id = :id""")
    suspend fun updateThumbnail(id:String)

    @Query("""UPDATE theme SET theme_resource_update_preload = 1 WHERE theme_id = :id""")
    suspend fun updatePreload(id:String)

    @Query("""UPDATE theme SET theme_resource_update_theme = 1 WHERE theme_id = :id""")
    suspend fun updateTheme(id:String)

    @Query("DELETE FROM theme WHERE theme_id = :themeId")
    suspend fun delete(themeId:String)
}