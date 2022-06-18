package com.fleet.knock.utils.admob

import android.app.Application
import com.fleet.knock.R

class PromotionADUtil private constructor(application: Application) : ADUtil(application, R.string.admob_promotion_ad_id){
    companion object{
        private var instance: ADUtil? = null
        fun get(application: Application) = instance
            ?: synchronized(this) {
                PromotionADUtil(application).also {
                    instance = it
                }
            }
    }
}