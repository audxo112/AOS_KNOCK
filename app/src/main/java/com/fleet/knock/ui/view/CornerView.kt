package com.fleet.knock.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.fleet.knock.R

class CornerView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){
        context.obtainStyledAttributes(attrs, R.styleable.CornerView).also{
            topCornerRadius = it.getDimension(R.styleable.CornerView_topCornerRadius, 0f)
            bottomCornerRadius = it.getDimension(R.styleable.CornerView_bottomCornerRadius, 0f)
        }.recycle()
    }

    var topCornerRadius:Float = 0f
        set(value){
            field = value

            background.apply{
                if(this is GradientDrawable)
                    cornerRadii = cornerRadiusList
            }
        }

    var bottomCornerRadius:Float = 0f
        set(value){
            field = value

            background.apply{
                if(this is GradientDrawable)
                    cornerRadii = cornerRadiusList
            }
        }



    private val cornerRadiusList
        get() = floatArrayOf(topCornerRadius, topCornerRadius, topCornerRadius, topCornerRadius, bottomCornerRadius, bottomCornerRadius, bottomCornerRadius, bottomCornerRadius)

    init{
        background = GradientDrawable().apply{
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#CC0E0E0E"))
            cornerRadii = cornerRadiusList
        }
    }
}