package com.fleet.knock.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class PagerRecyclerView : RecyclerView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var onPageSelectedListener:(current:Int, before:Int) -> Unit = {_,_-> }
    private var onPageScrolledListener:(current:Int, next:Int, offset:Float) -> Unit = {_,_,_ -> }

    private var viewWidth:Int? = null

    private var selectedChange = true
    private var selectedPos:Int = -1
    private var beforePos = -1

    private var _currentPos:Int = -1

    val currentPos
        get() = _currentPos

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val vw = if(viewWidth == null) {
                val manager = recyclerView.layoutManager?.let {
                    it as LinearLayoutManager
                } ?: return

                val firstPos = manager.findFirstVisibleItemPosition()
                manager.findViewByPosition(firstPos)?.width?.also{
                    viewWidth = it
                } ?: return
            }
            else viewWidth ?: return
            val itemCount = adapter?.itemCount ?: return

            val overallXScroll = recyclerView.computeHorizontalScrollOffset()

            val sPos = (overallXScroll + vw / 2) / vw
            if(selectedPos != sPos){
                selectedChange = true
                beforePos = selectedPos
                selectedPos = sPos
            }

            _currentPos = overallXScroll / vw
            val posOffset = (overallXScroll % vw).toFloat() / vw
            var nextPos = -1
            if(_currentPos + 1 < itemCount){
                nextPos = _currentPos + 1
            }

            onPageScrolledListener(_currentPos, nextPos, posOffset)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == 0 && selectedChange) {
                selectedChange = false
                onPageSelectedListener(selectedPos, beforePos)
            }
        }
    }

    init{
        PagerSnapHelper().attachToRecyclerView(this)
        layoutManager = LinearLayoutManager(context).apply{
            orientation = LinearLayoutManager.HORIZONTAL
        }
        addOnScrollListener(onScrollListener)
    }

    fun setOnPageSelectedListener(listener:(current:Int, before:Int) -> Unit){
        onPageSelectedListener = listener
    }

    fun setOnPageScrolled(listener: (current: Int, next: Int, offset: Float) -> Unit){
        onPageScrolledListener = listener
    }
}