package com.fleet.knock.utils.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpaceGridDivider(private val start:Int,
                       private val end:Int,
                       private val space:Int) : RecyclerView.ItemDecoration(){

    constructor(space:Int) : this(space, space, space)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val totalSpanCount = getTotalSpanCount(parent)
        val itemTotalCount = getItemTotalCount(parent)
        val spanSize = getItemSpanSize(parent, position)
        if (totalSpanCount == spanSize ||
            itemTotalCount == 0
        ) {
            return
        }
        outRect.top = if (isFirstRow(position, totalSpanCount)) start else space / 2
        outRect.left = if(isFirstInRow(position, totalSpanCount)) start else space / 2
        outRect.right = if(isLastInRow(position, totalSpanCount)) end else space / 2
        outRect.bottom = if (isLastRow(position, itemTotalCount, totalSpanCount)) end else space / 2
    }

    private fun isFirstRow(position: Int, spanCount: Int): Boolean {
        return position < spanCount
    }

    private fun isFirstInRow(position: Int, spanCount: Int): Boolean {
        return position % spanCount == 0
    }

    private fun isLastRow(position: Int, itemCount: Int, spanCount: Int): Boolean {
        return position >= itemCount - itemCount % spanCount
    }

    private fun isLastInRow(position: Int, spanCount: Int): Boolean {
        return isFirstInRow(position + 1, spanCount)
    }

    private fun getItemTotalCount(parent: RecyclerView): Int {
        val adapter = parent.adapter
        return adapter?.itemCount ?: 0
    }

    private fun getTotalSpanCount(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager) layoutManager.spanCount else 1
    }

    private fun getItemSpanSize(parent: RecyclerView, position: Int): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager) layoutManager.spanSizeLookup
            .getSpanSize(position) else 1
    }
}