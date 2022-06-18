package com.fleet.knock.ui.page.theme_list

import android.app.Application
import android.view.View
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class ThemeListRecentViewModel(application: Application) : BaseViewModel(application) {
    private val repository = FThemeRepository.get(application)

    val themeList = repository.getRecentThemeAll()

    val listVisibility = Transformations.map(themeList){
        if(it.isNotEmpty()) View.VISIBLE
        else View.INVISIBLE
    }

    val emptyVisibility = Transformations.map(themeList){
        if(it.isNotEmpty()) View.INVISIBLE
        else View.VISIBLE
    }

    suspend fun thumbnailDownload(t: FTheme){
        if(t.existThumbnail)
            return

        resourceDownload(t.getThumbnailProvider(getApplication()))

        t.existThumbnail = true
        repository.updatedThemeResourceThumbnail(t.id)
    }

    fun preloadDownload(theme: FTheme?){
        theme ?: return
        if(theme.existPreload)
            return

        viewModelScope.launch {
            resourceDownload(theme.getPreloadProvider(getApplication()))

            theme.existPreload = true
            repository.updatedThemeResourcePreload(theme.id)
        }
    }
}