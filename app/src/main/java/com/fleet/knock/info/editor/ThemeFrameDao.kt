package com.fleet.knock.info.editor

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface ThemeFrameDao {
    @Query("""
        SELECT * 
        FROM theme_frame 
        ORDER BY theme_frame_recent_used_time DESC, 
            theme_frame_priority DESC, 
            theme_frame_registered_time DESC
    """)
    suspend fun getAllSync() : List<ThemeFrame>

    @Query("SELECT * FROM theme_frame WHERE theme_frame_id = :id")
    suspend fun getSync(id:String) : ThemeFrame?

    @Query("""
        SELECT *
        FROM theme_frame f
        WHERE :time <= f.theme_frame_update_time + 2592000000 AND
        f.theme_frame_confirm_time < f.theme_frame_update_time
        LIMIT 1
    """)
    fun existConfirm(time:Date) : LiveData<ThemeFrame>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(frame:ThemeFrame)

    @Update
    suspend fun update(frame:ThemeFrame)

    @Query("""
        UPDATE theme_frame
        SET theme_frame_confirm_time = :time
        WHERE theme_frame_id = :id
    """)
    suspend fun updateConfirmTime(id:String, time:Date)

    @Query("""
        UPDATE theme_frame 
        SET theme_frame_recent_used_time = :time
        WHERE theme_frame_id = :id
    """)
    suspend fun updateRecentUsed(id:String, time: Date)

    @Query("""UPDATE theme_frame SET theme_frame_mini_thumbnail_update = 1 WHERE theme_frame_id = :id""")
    suspend fun updateMiniThumbnail(id:String)

    @Query("""UPDATE theme_frame SET theme_frame_thumbnail_update = 1 WHERE theme_frame_id = :id""")
    suspend fun updateThumbnail(id:String)

    @Query("""UPDATE theme_frame SET theme_frame_frame_update = 1 WHERE theme_frame_id = :id""")
    suspend fun updateFrame(id:String)

    @Transaction
    suspend fun replace(frame:ThemeFrame){
        val f = getSync(frame.id)
        if(f == null){
            insert(frame)
        }
        else{
            update(f.apply{
                update(frame)
            })
        }
    }

    @Query("DELETE FROM theme_frame WHERE theme_frame_id = :frameId")
    suspend fun delete(frameId:String)
}