package com.fleet.knock.utils

import android.app.Activity
import android.widget.Toast
import com.fleet.knock.R
import com.fleet.knock.ui.toast.SwapToast

class BackPressHandler {
    private var backKeyPressedTime = 0L

    fun onBackPressed(activity:Activity){
        if(activity.isFinishing)
            return

        if(System.currentTimeMillis() > backKeyPressedTime + 2000L){
            backKeyPressedTime = System.currentTimeMillis()

            Toast.makeText(activity, activity.getText(R.string.back_press_handler_request_back_button), Toast.LENGTH_SHORT).show()
        }
        else if(System.currentTimeMillis() <= backKeyPressedTime + 2000L){
            activity.finish()
        }
    }

    companion object{
        private var INSTANCE: BackPressHandler? = null
        fun get() = INSTANCE ?: synchronized(this){
            BackPressHandler().also{
                INSTANCE = it
            }
        }
    }
}