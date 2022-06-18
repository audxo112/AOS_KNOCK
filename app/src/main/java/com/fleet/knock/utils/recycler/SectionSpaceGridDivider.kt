package com.fleet.knock.utils.recycler

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SectionSpaceGridDivider(private val top:Int,
                              private val bottom:Int,
                              private val sectionDivider:Int,
                              private val itemSpace:Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val manager = parent.layoutManager.let{
            if(it is GridLayoutManager) it
            else null
        } ?: return

        val params = view.layoutParams.let{
            if(it is GridLayoutManager.LayoutParams) it
            else null
        } ?: return

        val position = parent.getChildAdapterPosition(view)

        outRect.top = if(isSection(manager, params))
            if(isFirstRow(position)) top
            else sectionDivider - itemSpace / 2
        else itemSpace / 2

        outRect.bottom = if(isLastRow(position, manager, parent)) bottom
        else itemSpace / 2

        outRect.left = if(isFirstCol(params)) 0
        else itemSpace / 2

        outRect.right = if(isLastCol(manager, params)) 0
        else itemSpace / 2
    }

    private fun isSection(manager:GridLayoutManager, params:GridLayoutManager.LayoutParams) : Boolean{
        return manager.spanCount == params.spanSize
    }

    private fun isFirstRow(position:Int) : Boolean{
        return position == 0
    }

    private fun isLastRow(position:Int, manager:GridLayoutManager, recyclerView: RecyclerView):Boolean{
        val itemCount = recyclerView.adapter?.itemCount ?: 0

        return getGroupIndex(position, manager) == getGroupIndex(itemCount -1, manager)
    }

    private fun getGroupIndex(position:Int, manager:GridLayoutManager) : Int{
        return manager.spanSizeLookup.getSpanGroupIndex(position, manager.spanCount)
    }

    private fun isFirstCol(params: GridLayoutManager.LayoutParams) : Boolean{
        return params.spanIndex == 0
    }

    private fun isLastCol(manager:GridLayoutManager, params: GridLayoutManager.LayoutParams) : Boolean{
        return params.spanIndex + params.spanSize == manager.spanCount
    }
}