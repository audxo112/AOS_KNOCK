package com.fleet.knock.utils

import android.app.Application
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*

class GAUtil private constructor(application: Application) {
    private val analytics by lazy {
        FirebaseAnalytics.getInstance(application)
    }

    private val config = PreferenceManager.getDefaultSharedPreferences(application)
    private val recentApply
        get() = config.getLong(CONFIG_RECENT_APPLY_DATE, System.currentTimeMillis())

    fun logEncoding(bundle:Bundle){
        analytics.logEvent(EVENT_ENCODING, bundle)
    }

    fun logApplyLocalTheme(pos:String){
        val recentApplyDate = (System.currentTimeMillis() - recentApply) / (24 * 60 * 60 * 1000)

        config.edit().apply{
            putLong(CONFIG_RECENT_APPLY_DATE, System.currentTimeMillis())
            apply()
        }

        analytics.logEvent(EVENT_APPLY_LOCAL_THEME, Bundle().apply {
            putString("위치", pos)
            putInt("최근변경일", recentApplyDate.toInt())
        })
    }

    fun logApplyTheme(){
        analytics.logEvent(EVENT_USING_THEME, Bundle())
    }

    fun logApplyFreeTheme(themeId:String, themeTitle:String){
        val recentApplyDate = (System.currentTimeMillis() - recentApply) / (24 * 60 * 60 * 1000)

        config.edit().apply{
            putLong(CONFIG_RECENT_APPLY_DATE, System.currentTimeMillis())
            apply()
        }

        analytics.logEvent(EVENT_APPLY_FREE_THEME, Bundle().apply {
            putString("ID", themeId)
            putString("제목", themeTitle)
            putInt("최근변경일", recentApplyDate.toInt())
        })
    }

    fun logUsingLocalTheme() {
        analytics.logEvent(EVENT_USING_LOCAL_THEME, Bundle())
    }

    fun logUsingFreeTheme(themeId: String, themeTitle: String) {
        analytics.logEvent(EVENT_USING_FREE_THEME, Bundle().apply {
            putString("ID", themeId)
            putString("제목", themeTitle)
        })
    }

    fun logUsingDefaultTheme(){
        analytics.logEvent(EVENT_USING_DEFAULT_THEME, null)
    }

    fun logSetWallpaper(pos: String, action: String) {
        analytics.logEvent(EVENT_SET_WALLPAPER, Bundle().apply {
            putString("위치", pos)
            putString("동작", action)
        })
    }

    fun logDownloadLocalTheme(){
        analytics.logEvent(EVENT_DOWNLOAD_LOCAL_THEME, Bundle())
    }

    fun logEnterThemeLink(themeId:String, themeTitle:String){
        analytics.logEvent(EVENT_ENTER_THEME_LINK, Bundle().apply {
            putString("ID", themeId)
            putString("제목", themeTitle)
        })
    }

    fun logEnterPromotionLink(promotionId:String, promotionTitle:String){
        analytics.logEvent(EVENT_ENTER_PROMOTION_LINK, Bundle().apply {
            putString("ID", promotionId)
            putString("제목", promotionTitle)
        })
    }

    companion object {
        private const val EVENT_ENCODING = "테마편집"
        const val ENCODING_TYPE_GIF = "Gif"
        const val ENCODING_TYPE_VIDEO = "영상"

        private const val EVENT_APPLY_LOCAL_THEME = "로컬_배경화면_적용"
        private const val EVENT_APPLY_FREE_THEME = "무료_배경화면_적용"
        const val APPLY_THEME_POS_EDIT = "편집"
        const val APPLY_THEME_POS_LOCAL = "내배경화면"

        private const val EVENT_USING_THEME = "배경화면_사용중"
        private const val EVENT_USING_LOCAL_THEME = "로컬_배경화면_사용중"
        private const val EVENT_USING_FREE_THEME = "무료_배경화면_사용중"
        private const val EVENT_USING_DEFAULT_THEME = "기본값_배경화면_사용중"

        private const val EVENT_SET_WALLPAPER = "테마설정"
        const val SET_WALLPAPER_POS_SETTING = "설정"
        const val SET_WALLPAPER_POS_LOCAL = "비공개테마"
        const val SET_WALLPAPER_POS_PROMOTION = "프로모션"

        private const val EVENT_DOWNLOAD_LOCAL_THEME = "다운로드_로컬_배경화면"

        const val SET_WALLPAPER_ACTION_SET = "설정"
        const val SET_WALLPAPER_ACTION_CLEAR = "해제"
        const val SET_WALLPAPER_ACTION_RESET = "재설정"

        private const val EVENT_ENTER_PROMOTION_LINK = "프로모션_링크"
        private const val EVENT_ENTER_THEME_LINK = "테마_링크"

        private const val CONFIG_RECENT_APPLY_DATE = "Config.RecentApplyDate"

        private var instance: GAUtil? = null
        fun get(application: Application) = instance ?: synchronized(this) {
            GAUtil(application).also {
                instance = it
            }
        }
    }

}