package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences

class TutorialPreviewSwipe(application: Application) : TutorialItem(application){
    override fun needTutorial(config: SharedPreferences): Boolean {
        return !config.getBoolean(NAME, false)
    }

    override fun progressTutorial(config: SharedPreferences) {
        config.edit().apply{
            putBoolean(NAME, true)
        }.apply()
    }

    companion object{
        const val NAME = "Tutorial.PreviewSwipe"
    }
}