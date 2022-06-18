package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.ViewConfiguration
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.fleet.knock.R

open class CompatDialog : DialogFragment(){

    protected var isAutoCancel = true
    protected var isTransparent = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(false)
            window?.apply {
                if(isTransparent) {
                    setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                    clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
                else
                    setBackgroundDrawable(ColorDrawable(getColor(R.color.colorBackgroundBrightBlackA60)))
            }
        }
    }

    override fun onPause() {
        if(isAutoCancel)
            dismiss()

        super.onPause()
    }

    private fun getColor(id:Int) : Int{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(id, null)
        else resources.getColor(id)
    }

    private fun getColorDrawable(id:Int) : ColorDrawable{
        return ColorDrawable(getColor(id))
    }


    fun getDrawable(id:Int) : Drawable? {
        return resources.getDrawable(id, null)
    }

    fun getHtml(id:Int) : Spanned{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(getString(id), Html.FROM_HTML_MODE_COMPACT)
        else{
            Html.fromHtml(getString(id))
        }
    }

    fun dpToPixel(dp:Int) : Float{
        return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getStaHeight(context:Context?) : Int{
        context ?: return 0
        val statusBarId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if(statusBarId > 0) context.resources.getDimension(statusBarId).toInt()
        else 0
    }

    fun getNavHeight() : Int{
        val resource = context?.resources ?: return 0
        val showNavId = resource.getIdentifier("config_showNavigationBar", "bool", "android")
        val showNav = if(showNavId > 0) resource.getBoolean(showNavId)
        else ViewConfiguration.get(context).hasPermanentMenuKey()

        return if(showNav){
            val navHeightId = resource.getIdentifier("navigation_bar_height", "dimen", "android")
            if(navHeightId > 0) resource.getDimension(navHeightId).toInt()
            else 0
        }
        else 0
    }
}