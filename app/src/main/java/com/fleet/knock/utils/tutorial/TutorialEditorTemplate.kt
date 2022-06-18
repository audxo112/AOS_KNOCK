package com.fleet.knock.utils.tutorial

import android.app.Application
import android.content.SharedPreferences
import com.fleet.knock.utils.dataloader.FDLFrame

class TutorialEditorTemplate(application: Application) : TutorialItem(application){
    override fun needTutorial(config: SharedPreferences): Boolean {
        return config.getLong(PARAM_LAST_UPDATED_TIME, 0L) <
                config.getLong(FDLFrame.CONFIG_RECENT_UPDATE_TIME, 0L)
    }

    override fun progressTutorial(config: SharedPreferences) {
        config.edit().apply{
            putLong(PARAM_LAST_UPDATED_TIME, config.getLong(FDLFrame.CONFIG_RECENT_UPDATE_TIME, 0L))
        }.apply()
    }

    companion object{
        const val NAME = "Tutorial.EditorTemplate"
        private const val PARAM_LAST_UPDATED_TIME = "$NAME.LastUpdatedTime"
    }
}