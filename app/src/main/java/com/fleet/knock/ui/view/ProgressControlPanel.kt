package com.fleet.knock.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ProgressControlPanel : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var progressBar:RangeProgressBar? = null

    private var touchStartX:Float = 0.0f
    private var startProgress:Long = 0L
    private val speed = 0.5f

    private var onTouchStartCallback:()->Unit = {}
    private var onTouchClearCallback:()->Unit = {}

    var preventTouch = false

    var interactiveEvent = true

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!interactiveEvent) return false
        if(preventTouch) return true

        event?: return false

        when(event.action){
            MotionEvent.ACTION_DOWN -> onTouchStart(event)
            MotionEvent.ACTION_MOVE -> progressBar?.setProgress(calculateProgress(event), true)
            MotionEvent.ACTION_UP -> onTouchClear()
        }

        return true
    }

    private fun onTouchStart(event:MotionEvent){
        onTouchStartCallback()

        touchStartX = event.x
        startProgress = progressBar?.progress ?: 0
    }

    private fun onTouchClear(){
        onTouchClearCallback()
    }

    private fun calculateProgress(event:MotionEvent) : Long{
        val progress = startProgress + ((event.x - touchStartX) * speed).toLong()
        return progressBar?.let{
            if(progress < it.start){
                touchStartX = event.x
                startProgress = it.start
                it.start
            }
            else if(progress > it.end){
                touchStartX = event.x
                startProgress = it.end
                it.end
            }
            else progress
        } ?: 0L
    }

    fun attachProgressBar(progressBar: RangeProgressBar){
        this.progressBar = progressBar
    }

    fun setOnTouchStartCallback(callback:()->Unit){
        onTouchStartCallback = callback
    }

    fun setOnTouchClearCallback(callback:()->Unit){
        onTouchClearCallback = callback
    }
}