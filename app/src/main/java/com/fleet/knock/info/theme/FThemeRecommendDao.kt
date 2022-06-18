package com.fleet.knock.info.theme

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface FThemeRecommendDao {

    @Transaction
    suspend fun getSync() : FTheme?{
        val cal = Calendar.getInstance()
        return getSync(1 shl (cal.get(Calendar.DAY_OF_WEEK) - 1), cal.time)?.also{
            recommendConfirm(it.id)
        }?: recommendReset().run{
            getSync(1 shl (cal.get(Calendar.DAY_OF_WEEK) - 1), cal.time)
        }
    }

    @Query("""SELECT t.*, u.*
        FROM (SELECT *
            FROM theme_recommend r
            WHERE (r.recommend_day & :todayDay) == :todayDay AND
            r.recommend_start_time < :todayTime AND r.recommend_end_time > :todayTime) r
        JOIN theme t ON r.recommend_theme_id = t.theme_id AND
        t.theme_resource_update_preload = 1 AND
        t.theme_resource_update_theme = 1
        JOIN user u ON t.theme_owner_uid = u.user_uid
        WHERE r.recommend_ready = 1
        ORDER BY Random()
        LIMIT 1""")
    suspend fun getSync(todayDay:Int, todayTime: Date) : FTheme?

    @Query("""SELECT t.*
        FROM theme t
        INNER JOIN theme_recommend r ON t.theme_id = r.recommend_theme_id
        WHERE t.theme_resource_update_theme = 0 OR
        t.theme_resource_update_preload = 0
        ORDER BY Random()
        LIMIT 1""")
    suspend fun getDownloadResource() : FThemeEntity?

    @Query("SELECT * FROM theme_recommend WHERE recommend_theme_id=:id")
    suspend fun get(id:String) : FThemeRecommend?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recommend:FThemeRecommend)

    @Transaction
    suspend fun replace(recommend: FThemeRecommend){
        val r = get(recommend.id)
        if(r == null){
            insert(recommend)
        }
        else{
            update(r.apply{
                update(recommend)
            })
        }
    }

    @Update
    suspend fun update(recommend:FThemeRecommend)

    @Query("UPDATE theme_recommend SET recommend_ready = 0 WHERE recommend_theme_id = :themeId")
    suspend fun recommendConfirm(themeId:String)

    @Query("UPDATE theme_recommend SET recommend_ready = 1")
    suspend fun recommendReset()

    @Query("DELETE FROM theme_recommend WHERE recommend_id = :recommendId")
    suspend fun delete(recommendId:String)
}