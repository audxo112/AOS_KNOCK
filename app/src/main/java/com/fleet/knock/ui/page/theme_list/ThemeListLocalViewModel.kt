package com.fleet.knock.ui.page.theme_list

import android.app.Application
import android.view.View
import androidx.lifecycle.Transformations
import com.fleet.knock.info.repository.FThemeLocalRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel

class ThemeListLocalViewModel(application: Application) : BaseViewModel(application){
    private val repository = FThemeLocalRepository.get(application)

    val themeList = repository.getThemeAll()
    val listVisibility = Transformations.map(themeList){
        if(it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }
    val emptyVisibility = Transformations.map(themeList){
        if(it.isNotEmpty()) View.INVISIBLE else View.VISIBLE
    }

    fun deleteItem(themeId:Long){
        repository.deleteTheme(getApplication(), themeId)
    }
}