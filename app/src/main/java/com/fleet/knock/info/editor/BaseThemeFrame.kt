package com.fleet.knock.info.editor

import android.graphics.Point
import com.fleet.knock.R

data class BaseThemeFrame(val screenSize:Point?,
                          val title:String,
                          val selectedResId:Int,
                          val unselectedResId:Int,
                          val ratio:String,
                          private val tMargin:Int,
                          private val bMargin:Int,
                          private val sMargin:Int,
                          private val eMargin:Int,
                          val verticalBias:Float = 0.5f,
                          val horizontalBias:Float = 0.5f) {

    constructor() : this(null, "기본", R.drawable.ic_editor_tool_frame_0_selected, R.drawable.ic_editor_tool_frame_0_unselected,"", 0, 0, 0, 0)

    fun topMargin(targetHeight:Int = -1) =
        if(targetHeight == -1 || screenSize == null) tMargin
        else tMargin * targetHeight / screenSize.y

    fun bottomMargin(targetHeight:Int = -1) =
        if(targetHeight == -1 || screenSize == null) bMargin
        else bMargin * targetHeight / screenSize.y

    fun startMargin(targetWidth:Int = -1) =
        if(targetWidth == -1 || screenSize == null) sMargin
        else sMargin * targetWidth / screenSize.x

    fun endMargin(targetWidth:Int = -1) =
        if(targetWidth == -1 || screenSize == null) eMargin
        else eMargin * targetWidth / screenSize.x
}