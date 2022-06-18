package com.fleet.knock.ui.fragment

import android.os.Build
import android.text.Html
import android.util.DisplayMetrics
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

abstract class CompatFragment(val TAG:String) : Fragment(){

    fun getColor(id:Int) = context?.let{
        ContextCompat.getColor(it, id)
    }

    fun getDrawable(id:Int) = context?.let{
        ContextCompat.getDrawable(it, id)
    }

    fun getHtml(id:Int) = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        Html.fromHtml(getString(id), Html.FROM_HTML_MODE_COMPACT)
    } else{
        Html.fromHtml(getString(id))
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun getNavHeight() : Int{
        val showNavId = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        val showNav = if(showNavId > 0) resources.getBoolean(showNavId)
        else ViewConfiguration.get(requireContext()).hasPermanentMenuKey()

        return if(showNav){
            val navHeightId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if(navHeightId > 0) resources.getDimension(navHeightId).toInt()
            else 0
        }
        else 0
    }
}