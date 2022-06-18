package com.fleet.knock.ui.toast

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewConfiguration
import android.widget.TextView
import android.widget.Toast
import com.fleet.knock.R

class KToast(private val context: Context) : Toast(context){
    private val messageView: TextView?

    var message:String = ""
        get() {
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                field
            }
            else messageView?.text.toString()
        }
        set(value){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                setText(value)
            }
            else{
                messageView?.text = value
            }
            field = value
        }

    var toastXOffset = 0
    var toastYOffset = 0

    init{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            messageView = null
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.toast_knock, null, false)
            messageView = view?.findViewById(R.id.toast_text)
        }
    }

    override fun show() {
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, toastXOffset, toastYOffset)
        super.show()
    }

    fun getStaHeight() : Int{
        val statusBarId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if(statusBarId > 0) context.resources.getDimension(statusBarId).toInt()
        else 0
    }

    fun getNavHeight() : Int{
        val showNavId = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        val showNav = if(showNavId > 0) context.resources.getBoolean(showNavId)
        else ViewConfiguration.get(context).hasPermanentMenuKey()

        return if(showNav){
            val navHeightId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if(navHeightId > 0) context.resources.getDimension(navHeightId).toInt()
            else 0
        }
        else 0
    }

    fun dpToPixel(dp:Int) = context.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}