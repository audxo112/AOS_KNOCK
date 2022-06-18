package com.fleet.knock.utils.recycler

import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import kotlin.system.measureTimeMillis

abstract class ExoPlayerViewHolder<T>(view: View) : BaseViewHolder<T>(view){
    protected val observer = Observer<Int?> {currentPos->
        val current = currentPos == adapterPosition
        if (play != current) {
            if (current) resume()
            else pause()

            play = current
        }
    }
    protected var play = false
        private set

    abstract fun resume()
    abstract fun pause()
}