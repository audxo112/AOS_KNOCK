package com.fleet.knock.ui.page.main

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.promotion.FPromotion
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.util.*

class MainPromotionHolderViewModel(application: Application) : BaseViewModel(application){
    private val repository = FThemeRepository.get(application)

    private var promotion:FPromotion? = null
    val promotionId
        get() = promotion?.id ?: ""


    val announce = MutableLiveData<String>()
    val bannerRatio = MutableLiveData<String>()

    private val isDownloadedBanner = MutableLiveData<Boolean>()
    val visibilityContent = Transformations.map(isDownloadedBanner){
        if(it) View.VISIBLE
        else View.INVISIBLE
    }
    val visibilityLoading = Transformations.map(isDownloadedBanner){
        if(it) View.INVISIBLE
        else View.VISIBLE
    }


    fun bind(p: FPromotion?){
        p ?: return

        promotion = p
        announce.value = p.announce
        bannerRatio.value = p.bannerRatio
        isDownloadedBanner.value = p.updateBanner
    }

    suspend fun bannerDownload(p:FPromotion){
        if(p.updateBanner)
            return

        resourceDownload(p.getBannerProvider(getApplication()))

        isDownloadedBanner.postValue(true)
        p.updateBanner = true
        p.updateBannerTime = Date()
        repository.updatePromotionBanner(p.id)
    }
}