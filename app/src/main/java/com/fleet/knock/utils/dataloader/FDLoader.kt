package com.fleet.knock.utils.dataloader

import android.content.Context
import android.content.SharedPreferences
import com.fleet.knock.info.database.KNOCKDatabase
import com.fleet.knock.info.mapper.Mapper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

abstract class FDLoader<T>(val name:String,
                           protected val mapper: Mapper<T>
){
    protected val crashlytics = FirebaseCrashlytics.getInstance()

    abstract suspend fun load(
        context: Context,
        fdb:FirebaseFirestore,
        db:KNOCKDatabase,
        config:SharedPreferences)

    protected fun getDate(config: SharedPreferences, key:String, defaultValue:Long = 0L) = Date(config.getLong(key, defaultValue))

    protected fun setDate(config: SharedPreferences, key:String, setValue:Long = System.currentTimeMillis()){
        config.edit().apply{
            putLong(key, setValue)
            apply()
        }
    }
}