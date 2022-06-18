package com.fleet.knock.ui.page.editor

import android.app.Application
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.editor.ThemeFrame
import com.fleet.knock.info.repository.FrameRepository
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.util.*

class EditorFrameTemplateHolderViewModel(application: Application) : BaseViewModel(application){
    private val repository = FrameRepository.get(application)

    var template:ThemeFrame? = null

    private val isOld = MutableLiveData<Boolean>()
    private val isConfirm = MutableLiveData<Boolean>()

    val visibilityIndicator = MediatorLiveData<Int>().apply{
        addSource(isOld){
            visibilityIndicator(it, isConfirm.value)
        }
        addSource(isConfirm){
            visibilityIndicator(isOld.value, it)
        }
    }

    private fun visibilityIndicator(isOld:Boolean?, isConfirm:Boolean?){
        isOld?:return
        isConfirm?:return

        visibilityIndicator.value =
            if(isOld || isConfirm) View.INVISIBLE
            else View.VISIBLE
    }

    fun bind(data:ThemeFrame?){
        data ?: return

        template = data

        isOld.value = System.currentTimeMillis() > data.updateTime.time + 2592000000L
        isConfirm.value = data.confirmTime >= data.updateTime
    }

    fun updateConfirmTime(){
        if(isConfirm.value == true)
            return

        val frame = template?:return
        viewModelScope.launch {
            val time = Date()
            frame.confirmTime = time
            isConfirm.value = true
            repository.updateFrameConfirmTime(frame.id, time)
        }
    }
}