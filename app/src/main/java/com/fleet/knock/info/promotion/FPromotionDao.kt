package com.fleet.knock.info.promotion

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FPromotionDao {
    @Query("""
        SELECT * 
        FROM promotion p
        WHERE (SELECT count(*) FROM theme t WHERE t.theme_promotion_id == p.promotion_id) > 0
        ORDER BY promotion_priority DESC, 
        promotion_registered_time DESC """)
    fun getAll() : LiveData<List<FPromotion>>

    @Query("""
        SELECT * 
        FROM promotion p
        WHERE (SELECT count(*) FROM theme t WHERE t.theme_promotion_id == p.promotion_id) > 0
        ORDER BY promotion_priority DESC, 
        promotion_registered_time DESC """)
    suspend fun getAllSync() : List<FPromotion>

    @Query("SELECT * FROM promotion WHERE promotion_id = :id")
    fun get(id:String) : LiveData<FPromotion>

    @Query("SELECT * FROM promotion WHERE promotion_id = :id")
    suspend fun getSync(id:String) : FPromotion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promotion:FPromotion)

    @Transaction
    suspend fun replace(promotion:FPromotion){
        val p = getSync(promotion.id)
        if(p == null){
            insert(promotion)
        }
        else{
            update(p.update(promotion))
        }
    }

    @Update
    suspend fun update(promotion:FPromotion)

    @Query("DELETE FROM promotion WHERE promotion_id = :id")
    suspend fun delete(id:String)

    @Query("UPDATE promotion SET promotion_update_banner = 1 WHERE promotion_id = :promotionId")
    suspend fun updateBanner(promotionId:String)

    @Query("UPDATE promotion SET promotion_update_main = 1 WHERE promotion_id = :promotionId")
    suspend fun updateMain(promotionId:String)
}