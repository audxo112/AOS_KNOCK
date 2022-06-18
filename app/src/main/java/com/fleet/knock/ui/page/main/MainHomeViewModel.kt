package com.fleet.knock.ui.page.main

import android.app.Application
import android.view.View
import androidx.lifecycle.Transformations
import com.fleet.knock.info.repository.FThemeLocalRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel

class MainHomeViewModel(application: Application) : BaseViewModel(application){
    private val repository = FThemeLocalRepository.get(application)

    val localTheme = repository.getThemeLimit(7)
    private val existLocalTheme = Transformations.map(localTheme){
        it.isNotEmpty()
    }
    val gotoEditorRatio = Transformations.map(existLocalTheme){
        if(it) "320:160"
        else "320:336"
    }

    val visibilityLocalTheme = Transformations.map(existLocalTheme){
        if(it) View.VISIBLE
        else View.GONE
    }
}