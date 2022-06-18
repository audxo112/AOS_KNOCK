package com.fleet.knock.info.converter

import androidx.room.TypeConverter

class StringSetConverter{
    @TypeConverter
    fun toStringSet(value:String?) : HashSet<String>{
        val set:HashSet<String> = HashSet()
        if(value != null && value.isNotEmpty()){
            set.addAll(value.split(',').map{ it.trim() }.filter{ it != "" })
        }
        return set
    }

    @TypeConverter
    fun toString(value:HashSet<String>?) : String{
        return value?.joinToString() ?: ""
    }
}
