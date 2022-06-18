package com.fleet.knock.utils

import android.app.Application
import androidx.preference.PreferenceManager

class FConfig private constructor(application : Application){
    private val config = PreferenceManager.getDefaultSharedPreferences(application)

    var isDoneEncode
        get() = config.getBoolean(IS_DONE_ENCODE, false)
        set(value) {
            config.edit().apply {
                putBoolean(IS_DONE_ENCODE, value)
            }.apply()
        }

    companion object{
        private var instance: FConfig? = null
        fun get(application: Application) = instance
            ?: synchronized(this){
                FConfig(application).also{
                    instance = it
                }
            }

        private const val IS_DONE_ENCODE = "IsDoneEncode"
    }
}