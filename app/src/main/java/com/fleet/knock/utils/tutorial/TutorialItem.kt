package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences

abstract class TutorialItem(protected val application: Application){
    fun requestTutorial(config:SharedPreferences) : Boolean{
        if(needTutorial(config)){
            progressTutorial(config)
            return true
        }
        return false
    }

    abstract fun needTutorial(config:SharedPreferences) : Boolean

    abstract fun progressTutorial(config:SharedPreferences)
}