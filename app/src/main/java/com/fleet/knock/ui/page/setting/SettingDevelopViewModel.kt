package com.fleet.knock.ui.page.setting

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.viewmodel.BaseViewModel

class SettingDevelopViewModel (application: Application) : BaseViewModel(application){

    private val encodingQualityOptimization = MutableLiveData(
        DevelopUtil.isEncodingQualityOptimization(application)
    )

    val encodingQualityOptimizationText = Transformations.map(encodingQualityOptimization){
        if(it) application.getText(R.string.activity_setting_develop_use)
        else application.getText(R.string.activity_setting_develop_not_use)
    }

    val encodingQualityOptimizationTextColor = Transformations.map(encodingQualityOptimization){
        if (it) ContextCompat.getColor(application, R.color.colorTextPrimary)
        else ContextCompat.getColor(application, R.color.colorTextGray)
    }

    private val encodingApplyAdBlock = MutableLiveData(
        DevelopUtil.isEncodingApplyAdBlock(application)
    )

    val encodingApplyAdBlockText = Transformations.map(encodingApplyAdBlock){
        if(it) application.getText(R.string.activity_setting_develop_use)
        else application.getText(R.string.activity_setting_develop_not_use)
    }

    val encodingApplyAdBlockTextColor = Transformations.map(encodingApplyAdBlock){
        if (it) ContextCompat.getColor(application, R.color.colorTextPrimary)
        else ContextCompat.getColor(application, R.color.colorTextGray)
    }

    private val localApplyAdBlock = MutableLiveData(
        DevelopUtil.isLocalApplyAdBlock(application)
    )

    val localApplyAdBlockText = Transformations.map(localApplyAdBlock){
        if(it) application.getText(R.string.activity_setting_develop_use)
        else application.getText(R.string.activity_setting_develop_not_use)
    }

    val localApplyAdBlockTextColor = Transformations.map(localApplyAdBlock){
        if (it) ContextCompat.getColor(application, R.color.colorTextPrimary)
        else ContextCompat.getColor(application, R.color.colorTextGray)
    }


    private val promotionApplyAdBlock = MutableLiveData(
        DevelopUtil.isPromotionApplyAdBlock(application)
    )

    val promotionApplyAdBlockText = Transformations.map(promotionApplyAdBlock){
        if(it) application.getText(R.string.activity_setting_develop_use)
        else application.getText(R.string.activity_setting_develop_not_use)
    }

    val promotionApplyAdBlockTextColor = Transformations.map(promotionApplyAdBlock){
        if (it) ContextCompat.getColor(application, R.color.colorTextPrimary)
        else ContextCompat.getColor(application, R.color.colorTextGray)
    }

    private fun setEncodingQualityOptimization(enable:Boolean){
        DevelopUtil.setEncodingQualityOptimization(getApplication(), enable)
        encodingQualityOptimization.value = enable
    }

    fun toggleEncodingQualityOptimization(){
        setEncodingQualityOptimization(encodingQualityOptimization.value == false)
    }

    private fun setEncodingApplyAdBlock(enable:Boolean){
        DevelopUtil.setEncodingApplyAdBlock(getApplication(), enable)
        encodingApplyAdBlock.value = enable
    }

    fun toggleEncodingApplyAdBlock(){
        setEncodingApplyAdBlock(encodingApplyAdBlock.value == false)
    }

    private fun setLocalApplyAdBlock(enable:Boolean){
        DevelopUtil.setLocalApplyAdBlock(getApplication(), enable)
        localApplyAdBlock.value = enable
    }

    fun toggleLocalApplyAdBlock(){
        setLocalApplyAdBlock(localApplyAdBlock.value == false)
    }

    private fun setPromotionApplyAdBlock(enable:Boolean){
        DevelopUtil.setPromotionApplyAdBlock(getApplication(), enable)
        promotionApplyAdBlock.value = enable
    }

    fun togglePromotionApplyAdBlock(){
        setPromotionApplyAdBlock(promotionApplyAdBlock.value == false)
    }
}