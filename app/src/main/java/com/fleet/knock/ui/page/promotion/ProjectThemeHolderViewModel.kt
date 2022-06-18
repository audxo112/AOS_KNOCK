package com.fleet.knock.ui.page.promotion

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.info.theme.FThemeEntity
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class ProjectThemeHolderViewModel(application: Application) : BaseViewModel(application){
    val repository = FThemeRepository.get(application)

    suspend fun thumbnailDownload(t:FThemeEntity){
        if(t.existThumbnail)
            return

        resourceDownload(t.getThumbnailProvider(getApplication()))

        t.existThumbnail = true
        repository.updatedThemeResourceThumbnail(t.id)
    }

    fun preloadDownload(theme:FThemeEntity?){
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