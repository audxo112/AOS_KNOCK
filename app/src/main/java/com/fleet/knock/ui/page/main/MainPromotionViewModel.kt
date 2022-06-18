package com.fleet.knock.ui.page.main

import android.app.Application
import android.view.View
import androidx.lifecycle.Transformations
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel

class MainPromotionViewModel(application: Application) : BaseViewModel(application){
    private val repository = FThemeRepository.get(application)

    private val recentThemeCount = repository.getRecentThemeAllCount()
    val visibilityRecent = Transformations.map(recentThemeCount){
        if (it > 0) View.VISIBLE
        else View.GONE
    }

    suspend fun getPromotionList() = repository.getPromotionAll()
}