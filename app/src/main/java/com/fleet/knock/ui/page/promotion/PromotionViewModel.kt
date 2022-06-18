package com.fleet.knock.ui.page.promotion

import android.app.Application
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.fleet.knock.info.promotion.FProject
import com.fleet.knock.info.promotion.FPromotion
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.info.theme.FThemeEntity
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.util.*

class PromotionViewModel(application: Application, val promotionId: String) : BaseViewModel(application) {
    val repository = FThemeRepository.get(application)

    val promotion = repository.getPromotion(promotionId)

    val promotionTitle = Transformations.map(promotion){
        it.title
    }

    val promotionMain = Transformations.map(promotion){
        if(it.updateMain) it.getMain(application)
        else {
            mainDownload(it)
            null
        }
    }

    val promotionMainVisibility = Transformations.map(promotion){
        if(it.enableMainBanner) View.VISIBLE
        else View.GONE
    }

    val promotionMainRatio = Transformations.map(promotion){
        it.mainRatio
    }

    val mainLink
        get() = promotion.value?.eventLink ?: ""

    fun logEnterPromotionLink(){
        val p = promotion.value ?: return
        GAUtil.get(getApplication())
            .logEnterPromotionLink(
                p.id,
                p.title
            )
    }

    val projectList = MutableLiveData<List<FProject>>()

    init{
        loadProjectList()
    }

    private fun loadProjectList(){
        viewModelScope.launch {
            projectList.value = repository.getProjectAll(promotionId)
        }
    }

    private fun mainDownload(promotion: FPromotion){
        if(promotion.updateMain)
            return

        viewModelScope.launch {
            resourceDownload(promotion.getMainProvider(getApplication()))

            promotion.updateMain = true
            repository.updatePromotionMain(promotion.id)
        }
    }

    class PromotionViewModelFactory(
        val application:Application,
        private val promotionId:String
    ):ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PromotionViewModel(application, promotionId) as T
        }
    }

    companion object {
        fun new(activity: AppCompatActivity, promotionId: String) = ViewModelProvider(
            activity,
            PromotionViewModelFactory(activity.application, promotionId))
            .get(PromotionViewModel::class.java)

    }
}