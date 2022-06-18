package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences

class TutorialEditorFrame(application: Application) : TutorialItem(application){
    override fun needTutorial(config: SharedPreferences)
            = config.getLong(PARAM_NEXT_TIME, 0L) < System.currentTimeMillis()

    override fun progressTutorial(config: SharedPreferences) {
        config.edit().apply{
            putLong(PARAM_NEXT_TIME, System.currentTimeMillis() + 86400000L)
        }.apply()
    }

    companion object{
        const val NAME = "Tutorial.EditorFrame"
        private const val PARAM_NEXT_TIME = "$NAME.NextTime"
    }
}