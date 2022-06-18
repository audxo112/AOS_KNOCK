package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences
import com.fleet.knock.utils.FConfig

class TutorialNaverCafe(application: Application) : TutorialItem(application){
    override fun needTutorial(config: SharedPreferences)
            = FConfig.get(application).isDoneEncode &&
            !config.getBoolean(NAME, false)

    override fun progressTutorial(config: SharedPreferences) {
        config.edit().apply{
            putBoolean(NAME, true)
        }.apply()
    }

    companion object{
        const val NAME = "Tutorial.NaverCafe"
    }
}