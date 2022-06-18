package com.fleet.knock.utils.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceHorizontalDivider (private val start:Int,
                              private val end:Int,
                              private val space:Int) : RecyclerView.ItemDecoration() {

    constructor(space:Int) : this(0, 0, space)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = parent.adapter?.itemCount ?: 0

        when(parent.getChildAdapterPosition(view)){
            0 -> {
                outRect.left = start
                outRect.right =
                    if(itemCount == 1) 0
                    else space / 2
            }
            itemCount - 1 -> {
                outRect.left =
                    if(itemCount == 1) 0
                    else space / 2
                outRect.right = end
            }
            else -> {
                outRect.left = space / 2
                outRect.right = space / 2
            }
        }
    }
}
