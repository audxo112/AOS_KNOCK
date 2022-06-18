package com.fleet.knock.ui.page.main

import android.app.Application
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : BaseViewModel(application){
    private val repository = FThemeRepository.get(application)

    private val uConfig = repository.getUserConfig()

    private val existSelectedTheme = Transformations.map(uConfig){
        it?.existSelectedTheme ?: false
    }
    val settingVisibility = Transformations.map(existSelectedTheme){
        if(it) View.VISIBLE
        else View.GONE
    }

    private val recommendNaverCafe = MutableLiveData(false)
    val visibilityRecommendNaverCafe = Transformations.map(recommendNaverCafe){
        if(it) View.VISIBLE
        else View.INVISIBLE
    }

    fun showRecommendNaverCafe(){
        recommendNaverCafe.value = true
    }

    fun hideRecommendNaverCafe(){
        recommendNaverCafe.value = false
    }

    var currentPage:String = PAGE_PROMOTION
        private set
    fun currentPageId(target:Int) = when(currentPage){
        PAGE_HOME -> R.id.open_home
        PAGE_PROMOTION -> R.id.open_promotion
        else -> target
    }
    fun isSamePage(page:String) = currentPage == page
    fun applyPage(page:String){
        currentPage = page
    }

    var isReview:Boolean = false
    init{
        val config = PreferenceManager.getDefaultSharedPreferences(application)
        val edit = config.edit()
        if(!config.contains(CONFIG_FIRST_ENTER_TIME)){
            edit.putLong(CONFIG_FIRST_ENTER_TIME, System.currentTimeMillis())
        }
        else if(!config.getBoolean(CONFIG_REVIEW, false)){
            if(config.getLong(CONFIG_FIRST_ENTER_TIME, 0L) + 2 * 86400000 < System.currentTimeMillis()){
                edit.putBoolean(CONFIG_REVIEW, true)
                isReview = true
            }
        }
        edit.apply()
    }

    companion object{
        const val PAGE_HOME = "Page.Home"
        const val PAGE_PROMOTION = "Page.Promotion"

        private const val CONFIG_FIRST_ENTER_TIME = "Config.FirstEnterTime"
        private const val CONFIG_REVIEW = "Config.Review"
    }
}