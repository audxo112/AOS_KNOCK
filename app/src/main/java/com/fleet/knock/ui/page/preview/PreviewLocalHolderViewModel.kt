package com.fleet.knock.ui.page.preview

import android.app.Application
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.R
import com.fleet.knock.info.repository.FThemeLocalRepository
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.info.user.UserConfig
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.StorageUtil
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PreviewLocalHolderViewModel(application: Application) : BaseViewModel(application) {
    private val repository = FThemeLocalRepository.get(application)

    var theme:FThemeLocal? = null

    fun logApplyFreeTheme(){
        GAUtil.get(getApplication()).logApplyLocalTheme(
            GAUtil.APPLY_THEME_POS_LOCAL
        )
    }

    fun bind(data:FThemeLocal?){
        data ?: return
        data.id ?: return

        theme = data
        themeId.value = data.id

        existSavedFile.value = StorageUtil.existVideo(
            getApplication<Application>().contentResolver,
            data.savedFileName
        )
    }

    private val uConfig = repository.getUserConfig()
    val themeId = MutableLiveData<Long>()

    val existSavedFile = MutableLiveData<Boolean>()
    val savedFileNameDrawable = Transformations.map(existSavedFile){
        if(it) ContextCompat.getDrawable(application,R.drawable.bg_dark_gray_r6_box)
        else ContextCompat.getDrawable(application,R.drawable.ripple_black_a10_r6_white_box)
    }
    val savedFileNameSrc = Transformations.map(existSavedFile){
        if(it) ContextCompat.getDrawable(application,R.drawable.ic_save_on_device_exist)
        else ContextCompat.getDrawable(application,R.drawable.ic_save_on_device)
    }

    val isSelectedTheme = MediatorLiveData<Boolean>().apply {
        addSource(uConfig) {
            isSelectedTheme(it, themeId.value)
        }
        addSource(themeId) {
            isSelectedTheme(uConfig.value, it)
        }
    }
    private fun isSelectedTheme(userConfig: UserConfig?, themeId:Long?){
        userConfig?: return
        themeId?: return

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
        if (it) ContextCompat.getDrawable(application, R.drawable.bg_dark_gray_r6_box)
        else ContextCompat.getDrawable(application, R.drawable.ripple_black_a10_r6_white_box)
    }

    fun selectLocalTheme() {
        val themeId = theme?.id ?: return
        viewModelScope.launch {
            repository.selectLocalTheme(themeId)
        }
    }

    fun deleteTheme(){
        val themeId = theme?.id ?: return
        repository.deleteTheme(getApplication(), themeId)
    }

    suspend fun saveTheme() : Boolean{
        val themeId = theme?.id ?: return false
        val provider = theme?.getThemeProvider(getApplication())
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss_SSS'.mp4'", Locale.KOREAN).format(Date())

        val result = withContext(Dispatchers.IO){
            StorageUtil.saveVideo(getApplication<Application>().contentResolver, provider?.file, fileName)
        }

        if(result){
            GAUtil.get(getApplication()).logDownloadLocalTheme()
            theme?.savedFileName = fileName
            existSavedFile.value = true
            repository.updateSavedFileName(themeId, fileName)
        }

        return result
    }
}