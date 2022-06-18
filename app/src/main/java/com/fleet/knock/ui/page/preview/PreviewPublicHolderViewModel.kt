package com.fleet.knock.ui.page.preview

import android.app.Application
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeRepository
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.info.user.User
import com.fleet.knock.info.user.UserConfig
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.LinkParser
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class PreviewPublicHolderViewModel(application: Application) : BaseViewModel(application) {
    private val repository = FThemeRepository.get(application)

    var theme:FTheme? = null

    private val userGrade = MutableLiveData<String>()
    val authUser = Transformations.map(userGrade){grade->
        when (grade) {
            User.GRADE_NORMAL -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }

    private val themeLink = MutableLiveData<String>()
    val themeLinkIcon = Transformations.map(themeLink){
        if(it == "") null
        else LinkParser.linkIconRes(it)
    }
    val themeLinkBackground = Transformations.map(themeLink){
        if(it == "") null
        else ContextCompat.getDrawable(application,R.drawable.bg_bright_black_a80_circle_btn)
    }
    val themeLinkRipple = Transformations.map(themeLink){
        if(it == "") null
        else ContextCompat.getDrawable(application,R.drawable.ripple_gray_a15_circle_btn)
    }

    fun logEnterThemeLink(){
        val t = theme ?: return
        GAUtil.get(getApplication()).logEnterThemeLink(
            t.id,
            t.themeTitle
        )
    }

    fun bind(data:FTheme?) {
        data ?: return

        theme = data
        themeId.value = data.id
        themeLink.value = data.themeLink
        isDownloadedTheme.value = data.existTheme
        userGrade.value = data.userGrade

        downloadProgress.value =
            if(data.existTheme) ""
            else getApplication<Application>().getString(R.string.view_preview_ready_download)
    }

    suspend fun preloadDownload(t:FTheme) {
        resourceDownload(t.getPreloadProvider(getApplication()))

        t.existPreload = true
        repository.updatedThemeResourcePreload(t.id)
    }

    suspend fun themeDownload(t:FTheme){
        if(t.existTheme)
            return

        resourceDownload(t.getThemeProvider(getApplication())){
            downloadProgress.postValue(it)
        }

        t.existTheme = true
        isDownloadedTheme.postValue(true)
        repository.updatedThemeResourceTheme(t.id)
    }

    suspend fun userAvatarDownload(user:User){
        if(user.updateAvatar)
            return

        resourceDownload(user.getAvatar(getApplication()))

        user.updateAvatar = true
        repository.updateUserAvatar(user.userUid)
    }

    private val userConfig = repository.getUserConfig()
    val themeId = MutableLiveData<String>()

    val isSelectedTheme = MediatorLiveData<Boolean>().apply {
        addSource(userConfig) {
            isSelectedTheme(it, themeId.value)
        }
        addSource(themeId) {
            isSelectedTheme(userConfig.value, it)
        }
    }
    private fun isSelectedTheme(userConfig: UserConfig?, themeId:String?){
        userConfig?: return
        themeId ?: return

        isSelectedTheme.value = userConfig.isSelectedTheme(themeId)
    }

    val applyThemeVisibility = Transformations.map(isSelectedTheme) {
        if (it) View.VISIBLE
        else View.INVISIBLE
    }
    val applyThemeText = Transformations.map(isSelectedTheme) {
        if (it) application.getString(R.string.view_preview_theme_remove)
        else application.getString(R.string.view_preview_theme_apply)
    }

    val applyThemeTextColor = Transformations.map(isSelectedTheme) {
        if (it) ContextCompat.getColor(application, R.color.colorTextBrightGray2)
        else ContextCompat.getColor(application, R.color.colorTextDarkGray)
    }

    val applyThemeRipple = Transformations.map(isSelectedTheme) {
        if (it) ContextCompat.getDrawable(application,R.drawable.ripple_gray_a15_l1_gray_r6_box)
        else ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_r6_white_box)
    }

    val downloadProgress =
        MutableLiveData(application.getString(R.string.view_preview_ready_download))

    val isDownloadedTheme = MutableLiveData<Boolean>()

    val visibilityDownloadProgress = Transformations.map(isDownloadedTheme){
        if(it) View.INVISIBLE
        else View.VISIBLE
    }

    fun selectPublicTheme() {
        val t = theme?: return
        viewModelScope.launch {
            repository.selectPublicTheme(t.id)
        }
    }
}