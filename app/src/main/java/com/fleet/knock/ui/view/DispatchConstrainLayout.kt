package com.fleet.knock.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class DispatchConstrainLayout : ConstraintLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var targetView: View? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val target = targetView ?: return false
        when(event.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_MOVE->
                target.dispatchTouchEvent(MotionEvent.obtain(
                    event.downTime,
                    event.eventTime,
                    event.action,
                    event.x,
                    target.y + target.height / 2,
                    event.metaState
                ))
        }
        return true
    }

    fun attachTargetView(view:View){
        targetView = view
    }
}