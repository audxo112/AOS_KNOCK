package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences

class TutorialEditorBoomerang(application: Application) : TutorialItem(application){
    override fun needTutorial(config: SharedPreferences)
            = config.getInt(PARAM_COUNT, 0) < 5 &&
                config.getLong(PARAM_NEXT_TIME, 0L) < System.currentTimeMillis()


    override fun progressTutorial(config: SharedPreferences) {
        config.edit().apply{
            putInt(PARAM_COUNT, config.getInt(PARAM_COUNT, 0) + 1)
            putLong(PARAM_NEXT_TIME, System.currentTimeMillis() + 86400000L)
        }.apply()
    }

    companion object{
        const val NAME = "Tutorial.EditorBoomerang"
        private const val PARAM_COUNT = "$NAME.Count"
        private const val PARAM_NEXT_TIME = "$NAME.NextTime"
    }
}