package com.fleet.knock.utils.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceVerticalDivider (private val top:Int,
                            private val bottom:Int,
                            private val space:Int) : RecyclerView.ItemDecoration() {

    constructor(space:Int) : this(0, 0, space)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val endPos = (parent.adapter?.itemCount ?: 1) - 1

        when(parent.getChildAdapterPosition(view)){
            0 -> {
                outRect.top = top
                outRect.bottom = space
            }
            endPos -> outRect.bottom = bottom
            else -> outRect.bottom = space
        }
    }
}
