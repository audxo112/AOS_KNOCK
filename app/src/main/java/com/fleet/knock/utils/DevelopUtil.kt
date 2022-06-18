package com.fleet.knock.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import java.util.*

object DevelopUtil {
    private val DEVELOP_DEVICE_IDS = listOf(
        "c47c4ec03315abe9",  // 명석 Note8 Debug
        "c3d71ea494aa76f3"   // 정현님 S20 Debug
    )

    fun isDeveloper(context: Context) : Boolean{
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//        Log.d("DEVELOP_TOOL", "deviceId: $deviceId")

        return DEVELOP_DEVICE_IDS.contains(deviceId)
    }

    private fun isEnable(context:Context, key:String, default:Boolean) : Boolean{
        val config = PreferenceManager.getDefaultSharedPreferences(context)
        return config.getBoolean(key, default)
    }

    private fun setEnable(context: Context, key:String, enable:Boolean){
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply{
            putBoolean(key, enable)
        }.apply()
    }

    fun isEncodingQualityOptimization(context:Context) : Boolean{
        return isEnable(context, ENCODING_QUALITY_OPTIMIZATION, isDeveloper(context))
    }

    fun setEncodingQualityOptimization(context:Context, enable:Boolean){
        setEnable(context, ENCODING_QUALITY_OPTIMIZATION, enable)
    }

    fun isEncodingApplyAdBlock(context:Context) : Boolean{
        return isEnable(context, ENCODING_APPLY_AD_BLOCK, isDeveloper(context))
    }

    fun setEncodingApplyAdBlock(context:Context, enable:Boolean){
        setEnable(context, ENCODING_APPLY_AD_BLOCK, enable)
    }

    fun isLocalApplyAdBlock(context:Context) : Boolean{
        return isEnable(context, LOCAL_APPLY_AD_BLOCK, isDeveloper(context))
    }

    fun setLocalApplyAdBlock(context:Context, enable:Boolean){
        setEnable(context, LOCAL_APPLY_AD_BLOCK, enable)
    }

    fun isPromotionApplyAdBlock(context:Context) : Boolean{
        return isEnable(context, PROMOTION_APPLY_AD_BLOCK, isDeveloper(context))
    }

    fun setPromotionApplyAdBlock(context:Context, enable:Boolean){
        setEnable(context, PROMOTION_APPLY_AD_BLOCK, enable)
    }

    private const val ENCODING_QUALITY_OPTIMIZATION = "Develop.EncodingQualityOptimization"
    private const val ENCODING_APPLY_AD_BLOCK = "Develop.EncodingApplyAdBlock"
    private const val LOCAL_APPLY_AD_BLOCK = "Develop.LocalApplyAdBlock"
    private const val PROMOTION_APPLY_AD_BLOCK = "Develop.PromotionApplyAdBlock"
}