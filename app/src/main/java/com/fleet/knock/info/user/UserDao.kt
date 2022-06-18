package com.fleet.knock.info.user

import androidx.room.*

@Dao
interface UserDao{
    @Query("SELECT * FROM user WHERE user_uid = :userUid")
    suspend fun getSync(userUid:String) : User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user:User)

    @Transaction
    suspend fun replace(user:User){
        val u = getSync(user.userUid)
        if(u == null){
            insert(user)
        }
        else{
            update(u.update(user))
        }
    }

    @Update
    suspend fun update(user:User)

    @Query("UPDATE user SET user_update_avatar = 1 WHERE user_uid = :userUid")
    suspend fun updateAvatar(userUid:String)

    @Query("DELETE FROM user WHERE user_uid = :userUid")
    suspend fun delete(userUid:String)
}