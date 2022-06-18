package com.fleet.knock.info.tag

import androidx.room.*

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tag:Tag)

    @Update
    fun update(tag:Tag)

    @Delete
    fun delete(tag:Tag)

    @Query("""SELECT * 
        FROM tag 
        WHERE instr(tag_name, :str) > 0 
        ORDER BY tag_count DESC""")
    fun get(str:String) : List<Tag>
}