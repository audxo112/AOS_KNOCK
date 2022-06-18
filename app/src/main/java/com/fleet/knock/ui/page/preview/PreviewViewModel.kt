package com.fleet.knock.ui.page.preview

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

open class PreviewViewModel (application: Application) : BaseViewModel(application){
    val isLoading = MutableLiveData(false)
    val visibilityLoading = Transformations.map(isLoading){
        if(it) View.VISIBLE
        else View.INVISIBLE
    }

    val currentDate = MutableLiveData<String>()
    val currentTime = MutableLiveData<String>()

    private val current = Date()
    private val dateFormat = SimpleDateFormat("M월 d일 E요일", Locale.KOREAN)
    private val timeFormat = SimpleDateFormat("h:mm", Locale.KOREAN)

    override fun onCleared() {
        stopClock()
    }

    private var clock: Job? = null

    fun startClock(){
        clock = viewModelScope.launch(Dispatchers.Default){
            while(isActive){
                current.time = System.currentTimeMillis()

                val date = dateFormat.format(current)
                if(currentDate.value != date)
                    currentDate.postValue(date)

                val time = timeFormat.format(current)
                if(currentTime.value != time)
                    currentTime.postValue(time)

                delay(60000L - current.time % 60000L)
            }
        }
    }

    fun stopClock(){
        clock?.cancel()
    }
}