package com.fleet.knock.ui.toast

import android.content.Context
import android.util.DisplayMetrics
import android.widget.Toast

class SwapToast private constructor(context: Context) {
    init {
        first = KToast(context).apply{
            toastYOffset = dpToPixel(190) + getStaHeight()
        }
        second = KToast(context).apply{
            toastYOffset = dpToPixel(190) + getStaHeight()
        }
    }

    private fun setContent(text:CharSequence, duration:Int){
        first?.message = text.toString()
        first?.duration = duration

        second?.message = text.toString()
        second?.duration = duration
    }

    fun show(){
        if(toggle) {
            first?.cancel()
            second?.show()
        }
        else{
            second?.cancel()
            first?.show()
        }
        toggle = !toggle
    }

    companion object{
        private var toggle = false
        private var first:KToast? = null
        private var second:KToast? = null

        private var instance:SwapToast? = null
        private fun getInstance(context: Context) = instance ?: synchronized(this) {
            SwapToast(context).also {
                instance = it
            }
        }

        fun makeText(context: Context, text:CharSequence, duration:Int = Toast.LENGTH_SHORT) : SwapToast {
            return getInstance(context).apply{
                setContent(text, duration)
            }
        }

        fun makeText(context:Context, textId:Int, duration:Int = Toast.LENGTH_SHORT) : SwapToast{
            return makeText(context, context.getText(textId), duration)
        }

        fun cancel(){
            toggle = false
            first?.cancel()
            second?.cancel()
        }
    }
}