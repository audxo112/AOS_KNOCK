package com.fleet.knock.info.tag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(@PrimaryKey(autoGenerate = false) @ColumnInfo(name = "tag_id") val id:String,
               @ColumnInfo(name = "tag_name") val name:String,
               @ColumnInfo(name = "tag_count") val count:Long)