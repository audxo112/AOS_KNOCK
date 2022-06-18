package com.fleet.knock.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.fleet.knock.R

class GradientTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){
        context.obtainStyledAttributes(attrs, R.styleable.GradientTextView).also{
            topCornerRadius = it.getDimension(R.styleable.GradientTextView_topCornerRadius, 0f)
            bottomCornerRadius = it.getDimension(R.styleable.GradientTextView_bottomCornerRadius, 0f)
            gradientColor = it.getColor(R.styleable.GradientTextView_gradientColor, Color.WHITE)
        }.recycle()
    }

    var topCornerRadius:Float = 0f
        set(value){
            for(i in 0..3)
                cornerRadiusList[i] = value
            field = value
            refreshBackgroundRadii()
        }

    var bottomCornerRadius:Float = 0f
        set(value){
            for(i in 4..7)
                cornerRadiusList[i] = value
            field = value
            refreshBackgroundRadii()
        }

    private fun refreshBackgroundRadii(){
        background.apply{
            if(this is GradientDrawable)
                cornerRadii = cornerRadiusList
        }
    }

    var gradientColor:Int = 0
        set(value) {
            field = value
            refreshBackgroundColor()
        }

    private fun refreshBackgroundColor(){
        background.apply {
            if (this is GradientDrawable)
                setColor(gradientColor)
        }
    }

    private val cornerRadiusList = floatArrayOf(topCornerRadius, topCornerRadius, topCornerRadius, topCornerRadius,
        bottomCornerRadius, bottomCornerRadius, bottomCornerRadius, bottomCornerRadius)

    init{
        background = GradientDrawable().apply{
            shape = GradientDrawable.RECTANGLE
            cornerRadii = cornerRadiusList
        }
    }
}