package com.fleet.knock.info.theme

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fleet.knock.info.user.User

@Dao
interface FThemeLocalDao {
    @Query("""SELECT * 
        FROM theme_local 
        WHERE local_shared = 0 AND
        (local_owner_uid = '${User.DEFAULT_UID}' OR
        local_owner_uid = :ownerUserUid)
        ORDER BY local_registered_time DESC""")
    fun getAll(ownerUserUid:String) : LiveData<List<FThemeLocal>>

    @Query("""SELECT * 
        FROM theme_local 
        WHERE local_shared = 0 AND
        (local_owner_uid = '${User.DEFAULT_UID}' OR
        local_owner_uid = :ownerUserUid)
        ORDER BY local_registered_time DESC""")
    suspend fun getAllSync(ownerUserUid:String) : List<FThemeLocal>

//    @Query("""SELECT *
//        FROM theme_local
//        WHERE local_shared = 0 AND
//        (local_owner_uid = '${User.DEFAULT_UID}' OR
//        local_owner_uid = :ownerUserUid)
//        ORDER BY local_registered_time DESC""")
//    suspend fun getAll(ownerUserUid:String) : List<FThemeLocal>

    @Query("""SELECT * 
        FROM theme_local 
        WHERE local_shared = 0 AND
        (local_owner_uid = '${User.DEFAULT_UID}' OR
        local_owner_uid = :ownerUserUid)
        ORDER BY local_registered_time DESC
        LIMIT :limit""")
    fun getLimit(ownerUserUid:String, limit:Int) : LiveData<List<FThemeLocal>>

    @Query("""SELECT * FROM theme_local
        WHERE (local_owner_uid = '${User.DEFAULT_UID}' OR
        local_owner_uid = :ownerUserUid) AND
        local_id = :themeId""")
    suspend fun getSync(ownerUserUid: String, themeId:Long) : FThemeLocal?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(local:FThemeLocal) : Long

    @Query("UPDATE theme_local SET local_saved_file_name = :fileName WHERE local_id = :id ")
    suspend fun updateSavedFileName(id:Long, fileName:String)

    @Query("UPDATE theme_local SET local_shared = 1 WHERE local_id = :id")
    suspend fun updateShared(id:Long)

    @Query("DELETE FROM theme_local WHERE local_id = :id")
    suspend fun delete(id:Long)
}