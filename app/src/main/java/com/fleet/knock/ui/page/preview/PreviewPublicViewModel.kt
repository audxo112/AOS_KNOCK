package com.fleet.knock.ui.page.preview

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.utils.GAUtil
import kotlinx.coroutines.launch

class PreviewPublicViewModel(application: Application,
                             private val page:String,
                             private val promotionId:String) : PreviewViewModel(application) {

    private val repository = FThemeRepository.get(getApplication())

    private val uConfig = repository.getUserConfig()

    private val selectedThemeId
        get() = uConfig.value?.selectedPublicThemeId ?: ""

    fun logSetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_PROMOTION,
            GAUtil.SET_WALLPAPER_ACTION_SET
        )
    }

    fun logResetWallpaper(){
        GAUtil.get(getApplication()).logSetWallpaper(
            GAUtil.SET_WALLPAPER_POS_PROMOTION,
            GAUtil.SET_WALLPAPER_ACTION_RESET
        )
    }

    fun logApplyFreeTheme(){
        viewModelScope.launch {
            val theme = repository.getThemeSync(selectedThemeId) ?: return@launch

            GAUtil.get(getApplication()).logApplyFreeTheme(
                theme.id,
                theme.themeTitle
            )
        }
    }

    suspend fun getThemeList() = when(page){
        PreviewPublicActivity.PAGE_PROJECT -> repository.getThemeAllInPromotionSync(promotionId)
        PreviewPublicActivity.PAGE_RECENT -> repository.getRecentThemeAllSync()
        else -> repository.getThemeAllSync()
    }

    private var currentPage = PAGE_CONTENT
    fun applyPage(page:String){
        if(page == PreviewLocalViewModel.PAGE_LOCK_PREVIEW)
            startClock()
        else
            stopClock()

        currentPage = page
    }

    val nextPreviewPage
        get() = when(currentPage){
            PAGE_CONTENT -> PAGE_HOME_PREVIEW
            PAGE_HOME_PREVIEW -> PAGE_LOCK_PREVIEW
            PAGE_LOCK_PREVIEW -> PAGE_CONTENT
            PAGE_DETAIL_CONTENT -> PAGE_CONTENT
            else -> null
        }

    val togglePage
        get() = when(currentPage){
            PAGE_CONTENT -> PAGE_DETAIL_CONTENT
            PAGE_DETAIL_CONTENT -> PAGE_CONTENT
            else -> null
        }

    fun isFadeAnim(page:String = currentPage) = when(page){
        PAGE_CONTENT,
        PAGE_DETAIL_CONTENT-> false
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

    class PreviewViewModelFactory(private val application: Application,
                                  private val page:String,
                                  private val promotionId: String = "") : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PreviewPublicViewModel(application, page, promotionId) as T
        }
    }

    companion object {
        fun new(
            activity: AppCompatActivity,
            page: String,
            id:String = ""
        ) = ViewModelProvider(
            activity,
            PreviewViewModelFactory(activity.application, page, id)
        ).get(PreviewPublicViewModel::class.java)

        const val PAGE_CONTENT = "Page.Content"
        const val PAGE_DETAIL_CONTENT = "Page.DetailContent"
        const val PAGE_HOME_PREVIEW = "Page.HomePreview"
        const val PAGE_LOCK_PREVIEW = "Page.LockPreview"
    }
}