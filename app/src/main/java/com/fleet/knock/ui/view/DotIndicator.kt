package com.fleet.knock.ui.view

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

class DotIndicator : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val dots = ArrayList<ImageView>()
    private var pagerRecycler : PagerRecyclerView? = null

    private var dotSize = dpToPixel(8)
    private var dotSpacing = dpToPixel(10)
    private var dotCornerRadius = dotSize / 2

    private var dotWidthFactor = DEFAULT_WIDTH_FACTOR
    private var dotColor = DEFAULT_DOT_COLOR
    private var selectedDotColor = DEFAULT_DOT_COLOR

    private val argbEvaluator = ArgbEvaluator()

    init{
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        orientation = HORIZONTAL
    }

    private fun refreshDots(){
        if(pagerRecycler?.adapter != null) {
            post {
                refreshDotsCount()
                refreshDotsColors()
                refreshDotsSelected()
                refreshDotsSize()
            }
        }
    }

    private fun refreshDotsSelected(){
        Log.d("TEST_INDICATOR", "dot selected : ${dots.size}")
        val currentPos = pagerRecycler?.currentPos ?: return
        val adapter = pagerRecycler?.adapter ?: return
        onPageScrolled(currentPos,
            if(currentPos + 1 < adapter.itemCount) currentPos + 1
            else currentPos,
            0.0f)
    }

    private fun refreshDotsCount(){
        Log.d("TEST_INDICATOR", "dot count : ${dots.size}")
        val adapter = pagerRecycler?.adapter ?: return
        if(dots.size < adapter.itemCount){
            addDots(adapter.itemCount - dots.size)
        }
        else if(dots.size > adapter.itemCount){
            removeDots(dots.size - adapter.itemCount)
        }
    }

    private fun addDots(count:Int){
        for(i in 0 until count){
            val dot = ImageView(context).apply{
                layoutParams = MarginLayoutParams(dotSize, dotSize).apply{
                    setMargins(dotSpacing / 2, 0, dotSpacing / 2, 0)
                }
                background = GradientDrawable().apply{
                    shape = GradientDrawable.RECTANGLE
                    setColor(dotColor)
                    cornerRadius = dotCornerRadius.toFloat()
                }
                isFocusable = false
                isFocusableInTouchMode = false
            }
            dots.add(dot)
            addView(dot)
        }
    }

    private fun removeDots(count:Int){
        for(i in 0 until count){
            removeViewAt(childCount - 1)
            dots.removeAt(dots.size - 1)
        }
    }

    private fun refreshDotsColors(){
        val helper = pagerRecycler ?: return
        for((index, dot) in dots.withIndex()){
            (dot.background as GradientDrawable).setColor(
                if(index == helper.currentPos) selectedDotColor
                else dotColor
            )
            dot.invalidate()
        }
    }


    private fun refreshDotsSize(){
        val helper = pagerRecycler ?: return
        if(helper.currentPos == -1) return

        for(i in 0 until helper.currentPos) {
            dots[i].layoutParams = dots[i].layoutParams.apply {
                width = dotSize
            }
        }
    }

    fun setDotColor(color:Int){
        dotColor = color
    }

    fun setSelectedDotColor(color:Int){
        selectedDotColor = color
    }

    fun attachPagerRecyclerView(pager: PagerRecyclerView){
        pagerRecycler = pager.apply {
            setOnPageScrolled(onPageScrolled)
        }
        refreshDots()
    }

    private val onPageScrolled = onPageScrolled@{current:Int, next:Int, offset:Float ->
        if(current < 0 || current >= dots.size) return@onPageScrolled

        val currentDot = dots[current]
        currentDot.layoutParams = currentDot.layoutParams.apply{
            width = (dotSize + (dotSize * dotWidthFactor - 1) * (1 - offset)).toInt()
        }
        if(selectedDotColor != dotColor){
            (currentDot.background as GradientDrawable).apply{
                setColor(argbEvaluator.evaluate(offset, selectedDotColor, dotColor) as Int)
            }
        }

        currentDot.invalidate()

        if(next == -1 || current == next) return@onPageScrolled

        val nextDot = dots[next]
        nextDot.layoutParams = nextDot.layoutParams.apply {
            width = (dotSize + (dotSize * dotWidthFactor - 1) * offset).toInt()
        }
        if(selectedDotColor != dotColor){
            (nextDot.background as GradientDrawable).apply{
                setColor(argbEvaluator.evaluate(offset, dotColor, selectedDotColor) as Int)
            }
        }

        nextDot.invalidate()
    }

    private fun dpToPixel(dp: Int): Int {
        val dm = Resources.getSystem().displayMetrics
        return (dp * (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }


    companion object{
        private val DEFAULT_DOT_COLOR = Color.parseColor("#C4C4C4")
        const val DEFAULT_WIDTH_FACTOR = 2f
    }
}