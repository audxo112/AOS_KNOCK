package com.fleet.knock.utils.admob

import android.app.Application
import com.fleet.knock.R

class BaseADUtil private constructor(application: Application) : ADUtil(application, R.string.admob_base_ad_id){
    companion object{
        private var instance: ADUtil? = null
        fun get(application: Application) = instance
            ?: synchronized(this) {
            BaseADUtil(application).also {
                instance = it
            }
        }
    }
}