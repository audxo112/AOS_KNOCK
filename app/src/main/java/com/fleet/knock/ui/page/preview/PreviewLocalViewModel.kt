package com.fleet.knock.ui.page.preview

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeLocalRepository
import com.fleet.knock.utils.GAUtil
import kotlinx.coroutines.launch

class PreviewLocalViewModel(application: Application) : PreviewViewModel(application) {
    private val repository = FThemeLocalRepository.get(application)

    private val uConfig = repository.getUserConfig()

    private val selectedThemeId
        get() = uConfig.value?.selectedLocalThemeId ?: -1L

    fun logSetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_LOCAL,
            GAUtil.SET_WALLPAPER_ACTION_SET
        )
    }

    fun logResetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_LOCAL,
            GAUtil.SET_WALLPAPER_ACTION_RESET
        )
    }

    fun logApplyLocalTheme(){
        GAUtil.get(getApplication()).logApplyLocalTheme(
            GAUtil.APPLY_THEME_POS_LOCAL
        )
    }

    suspend fun getThemeList() = repository.getThemeAllSync()

    private var currentPage = PAGE_CONTENT
    fun impossibleTransition(page:String) = currentPage == page
    fun applyPage(page:String){
        if(page == PAGE_LOCK_PREVIEW)
            startClock()
        else
            stopClock()

        currentPage = page
    }

    val nextPage
        get() = when(currentPage){
            PAGE_CONTENT -> PAGE_HOME_PREVIEW
            PAGE_HOME_PREVIEW -> PAGE_LOCK_PREVIEW
            PAGE_LOCK_PREVIEW -> PAGE_CONTENT
            else -> null
        }

    fun isFadeAnim(page:String = currentPage) = when(page){
        PAGE_CONTENT -> false
        else -> true
    }

    fun restoreSelectTheme(){
        viewModelScope.launch {
            repository.restoreSelectTheme()
        }
    }

    fun removeSelectTheme(){
        viewModelScope.launch {
            repository.removeSelectTheme()
        }
    }

    companion object{
        const val PAGE_CONTENT = "Page.Content"
        const val PAGE_HOME_PREVIEW = "Page.HomePreview"
        const val PAGE_LOCK_PREVIEW = "Page.LockPreview"
    }
}