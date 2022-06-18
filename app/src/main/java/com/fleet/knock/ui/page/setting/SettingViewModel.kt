package com.fleet.knock.ui.page.setting

import android.app.Application
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.WallpaperUtil
import com.fleet.knock.utils.viewmodel.BaseViewModel

class SettingViewModel(application: Application) : BaseViewModel(application) {
    private val repository = FThemeRepository.get(application)

    fun logSetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_SETTING,
            GAUtil.SET_WALLPAPER_ACTION_SET
        )
    }

    fun logResetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_SETTING,
            GAUtil.SET_WALLPAPER_ACTION_RESET
        )
    }

    fun logClearWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_SETTING,
            GAUtil.SET_WALLPAPER_ACTION_CLEAR
        )
    }

    var isCreated = true

    val isLoading = MutableLiveData(false)
    val visibilityLoading = Transformations.map(isLoading){
        if(it) View.VISIBLE
        else View.INVISIBLE
    }

    private val existSelectedTheme = Transformations.map(repository.getUserConfig()) {
        it?.existSelectedTheme ?: false
    }
    val isSetWallpaper = MutableLiveData(
        WallpaperUtil.isSetWallpaper(application)
    )

    val enableTheme = MediatorLiveData<Boolean>().apply {
        addSource(existSelectedTheme){
            enableTheme(it, isSetWallpaper.value)
        }
        addSource(isSetWallpaper){
            enableTheme(existSelectedTheme.value, it)
        }
    }

    private fun enableTheme(existSelected:Boolean?, isSetWallpaper:Boolean?){
        existSelected ?: return
        isSetWallpaper ?: return

        val enable = existSelected && isSetWallpaper
        if(enableTheme.value != enable)
            enableTheme.value = enable
    }

    val enableBadgeVisibility = Transformations.map(enableTheme) { enable ->
        if (enable) View.INVISIBLE
        else View.VISIBLE
    }
    val enableThemeUseTextColor = Transformations.map(enableTheme) { enable ->
        if (enable) ContextCompat.getColor(application, R.color.colorTextPrimary)
        else ContextCompat.getColor(application, R.color.colorTextGray)
    }
    val enableThemeUseText = Transformations.map(enableTheme) { enable ->
        if (enable) application.getText(R.string.activity_setting_enable_theme_use)
        else application.getText(R.string.activity_setting_enable_theme_not_use)
    }

    private val isDeveloper = MutableLiveData(DevelopUtil.isDeveloper(application))
    val visibilityDevelopTool = Transformations.map(isDeveloper){
        if(it) View.VISIBLE
        else View.GONE
    }
}
