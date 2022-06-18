package com.fleet.knock.info.promotion

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.info.theme.FThemeEntity
import java.util.*

@Dao
interface FProjectDao {
    @Transaction
    @Query("""
        SELECT * 
        FROM promotion_project p
        WHERE project_promotion_id = :promotionId AND
            (SELECT count(*) 
            FROM theme t 
            WHERE t.theme_project_id == p.project_id AND 
                t.theme_post_start_time <= :time AND
                t.theme_post_end_time >= :time) > 0 
        ORDER BY project_priority DESC, 
        project_registered_time DESC """)
    fun getAll(promotionId:String, time:Date) : LiveData<List<FProject>>

    @Transaction
    @Query("""
        SELECT * 
        FROM promotion_project p
        WHERE project_promotion_id = :promotionId AND
            (SELECT count(*) 
            FROM theme t 
            WHERE t.theme_project_id == p.project_id AND 
                t.theme_post_start_time <= :time AND
                t.theme_post_end_time >= :time) > 0 
        ORDER BY project_priority DESC, 
        project_registered_time DESC """)
    suspend fun getAllSync(promotionId:String, time:Date) : List<FProject>

    @Query("""SELECT *
        FROM theme t
        WHERE
            t.theme_project_id = :projectId AND
            t.theme_allow_post = 1 AND
            t.theme_post_start_time <= :time AND
            t.theme_post_end_time >= :time
        ORDER BY t.theme_like_count, t.theme_registered_time DESC""")
    suspend fun getThemeAllSync(projectId:String, time:Date) : List<FThemeEntity>

    @Transaction
    suspend fun getAllWithThemeSync(promotionId: String, time:Date) : List<FProject>{
        val pList = getAllSync(promotionId, time)
        for(p in pList){
            p.themes.addAll(getThemeAllSync(p.projectId, time))
        }
        return pList
    }

    @Query("SELECT * FROM promotion_project WHERE project_id = :projectId")
    suspend fun getSync(projectId:String) : FProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project:FProjectEntity)

    @Transaction
    suspend fun replace(project:FProjectEntity){
        val p = getSync(project.id)
        if(p == null){
            insert(project)
        }
        else{
            update(p.update(project))
        }
    }

    @Update
    suspend fun update(project:FProjectEntity)

    @Query("DELETE FROM promotion_project WHERE project_id = :projectId")
    suspend fun delete(projectId:String)
}