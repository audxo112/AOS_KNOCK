package com.fleet.knock.utils.tutorial

import android.app.Application
import androidx.preference.PreferenceManager

class TutorialUtil private constructor(){
    private val tutorialMap = hashMapOf<String, TutorialItem>()

    private fun build(application: Application, name:String) : TutorialItem?{
        if(tutorialMap.containsKey(name)){
            return tutorialMap[name]
        }

        val item = when(name){
            TutorialEditorBoomerang.NAME -> TutorialEditorBoomerang(application)
            TutorialEditorFrame.NAME -> TutorialEditorFrame(application)
            TutorialEditorTemplate.NAME -> TutorialEditorTemplate(application)
            TutorialEditorTrim.NAME -> TutorialEditorTrim(application)
            TutorialNaverCafe.NAME -> TutorialNaverCafe(application)
            TutorialPreviewSwipe.NAME -> TutorialPreviewSwipe(application)
            else -> null
        }
        if(item != null){
            tutorialMap[name] = item
        }

        return item
    }

    fun need(application: Application, name: String) : Boolean{
        val tutorialItem = build(application, name) ?: return false

        return tutorialItem.needTutorial(
            PreferenceManager.getDefaultSharedPreferences(application)
        )
    }

    fun complete(application: Application, name:String){
        val tutorialItem = build(application, name) ?: return

        tutorialItem.progressTutorial(
            PreferenceManager.getDefaultSharedPreferences(application)
        )
    }

    fun request(application: Application, name:String) : Boolean{
        val tutorialItem = build(application, name) ?: return false

        return tutorialItem.requestTutorial(
            PreferenceManager.getDefaultSharedPreferences(application)
        )
    }

    companion object{
        private var instance: TutorialUtil? = null
        fun get() = instance
            ?: synchronized(this){
            TutorialUtil().also{
                instance = it
            }
        }
    }
}