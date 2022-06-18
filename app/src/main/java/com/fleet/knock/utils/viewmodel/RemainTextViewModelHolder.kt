package com.fleet.knock.utils.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R

class RemainTextViewModelHolder (val maxLength:Int){
    val text = MutableLiveData<String>("")
    val remain = Transformations.map(text){
        "${maxLength - (it.length)}자"
    }
    val remainColorRes = Transformations.map(text){
        if(it.length >= maxLength) R.color.colorTextRed else R.color.colorTextGray
    }
}