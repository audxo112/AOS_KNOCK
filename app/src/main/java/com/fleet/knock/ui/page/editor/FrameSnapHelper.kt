package com.fleet.knock.ui.page.editor

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

//class FrameSnapHelper : LinearSnapHelper(){
//    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
//        if(layoutManager is LinearLayoutManager){
//            if(needToDoSnap(layoutManager))
//        }
//
//        return super.findSnapView(layoutManager)
//    }
//
//    private fun needToDoSnap(layoutManager: RecyclerView.LayoutManager?) : Boolean{
//        layoutManager ?: return false
//        if(layoutManager !is LinearLayoutManager){
//            return false
//        }
//        return layoutManager.findFirstCompletelyVisibleItemPosition() != 0 &&
//                layoutManager.findLastCompletelyVisibleItemPosition() != layoutManager.itemCount -1
//    }
//}

class FrameSnapHelper : LinearSnapHelper(){

    private var horizontalHelper:OrientationHelper? = null

    private fun getHorizontalHelper(layoutManager:RecyclerView.LayoutManager) : OrientationHelper? {
        if(horizontalHelper == null || horizontalHelper?.layoutManager !== layoutManager){
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return horizontalHelper
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        val helper = getHorizontalHelper(layoutManager)
        val out = IntArray(2){ 0 }

        if(helper != null)
            out[0] = distanceToCenter(layoutManager, targetView, helper)

        return out
    }

    private fun distanceToCenter(layoutManager:RecyclerView.LayoutManager, targetView:View, helper: OrientationHelper) : Int{
        val childCenter = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter =
            if(layoutManager.clipToPadding)
                helper.startAfterPadding + helper.totalSpace / 2
            else
                helper.end / 2
        return childCenter - containerCenter
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager?,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if(layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider)
            return RecyclerView.NO_POSITION

        val itemCount = layoutManager.itemCount
        if(itemCount == 0)
            return RecyclerView.NO_POSITION

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)

        if(currentPosition == RecyclerView.NO_POSITION)
            return RecyclerView.NO_POSITION

        val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
        val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1) ?: return RecyclerView.NO_POSITION

        var hDeltaJump = 0
        if(layoutManager.canScrollHorizontally()){
            hDeltaJump = estimateNextPositionDiffForFling(layoutManager, getHorizontalHelper(layoutManager), velocityX)
            if(vectorForEnd.x < 0)
                hDeltaJump = -hDeltaJump
        }

        if(hDeltaJump == 0)
            return RecyclerView.NO_POSITION

        var targetPos = currentPosition + hDeltaJump
        if(targetPos < 0)
            targetPos = 0

        if(targetPos >= itemCount){
            targetPos = itemCount - 1
        }

        return targetPos
    }

    private fun estimateNextPositionDiffForFling(
        layoutManager:RecyclerView.LayoutManager,
        helper:OrientationHelper?,
        velocityX:Int) : Int{
        helper ?: return 0

        val distances = calculateScrollDistance(velocityX, 0)
        val distancePerChild = computeDistancePerChild(layoutManager, helper)
        if(distancePerChild <= 0)
            return 0

        val distance =
            if(abs(distances[0]) > abs(distances[1])) distances[0]
            else distances[1]

        return round(distance / distancePerChild).toInt()
    }

    private fun computeDistancePerChild(layoutManager:RecyclerView.LayoutManager, helper:OrientationHelper) : Float{
        var minPosView:View? = null
        var maxPosView:View? = null
        var minPos = Integer.MAX_VALUE
        var maxPos = Integer.MIN_VALUE

        val childCount = layoutManager.childCount
        if(childCount == 0)
            return INVALID_DISTANCE

        for(i in 0 until childCount){
            val child = layoutManager.getChildAt(i) ?: continue
            val pos = layoutManager.getPosition(child)
            if(pos == RecyclerView.NO_POSITION)
                continue

            if(pos < minPos){
                minPos = pos
                minPosView = child
            }

            if(pos > maxPos){
                maxPos = pos
                maxPosView = child
            }
        }

        if(minPosView == null || maxPosView == null)
            return INVALID_DISTANCE

        val start = min(helper.getDecoratedStart(minPosView), helper.getDecoratedStart(maxPosView))
        val end = max(helper.getDecoratedEnd(minPosView), helper.getDecoratedEnd(maxPosView))
        val distance = end - start

        return if(distance == 0) INVALID_DISTANCE
        else distance.toFloat() / (maxPos - minPos + 1)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        layoutManager ?: return null
        if(layoutManager.canScrollHorizontally()){
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun findCenterView(layoutManager:RecyclerView.LayoutManager?, helper:OrientationHelper?) : View?{
        layoutManager ?: return null
        helper ?: return null

        if(layoutManager is LinearLayoutManager){
            if(layoutManager.findFirstCompletelyVisibleItemPosition() == 0)
                return layoutManager.getChildAt(0)
            else if(layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1)
                return layoutManager.findViewByPosition(layoutManager.itemCount - 1)
        }

        val childCount = layoutManager.childCount
        if(childCount == 0)
            return null

        var closestChild:View? = null
        val center:Int =
            if(layoutManager.clipToPadding)
                helper.startAfterPadding + helper.totalSpace / 2
            else helper.end / 2

        var absClosest = Integer.MAX_VALUE

        for(i in 0 until childCount){
            val child = layoutManager.getChildAt(i)
            val childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = abs(childCenter - center)

            if(absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    fun getSnapPosition(recyclerView:RecyclerView) : Int{
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

        return layoutManager.getPosition(snapView)
    }

    private var currentSnapPosition = -1

    private var onSnapPositionChange:(position:Int)->Unit = {}

    fun setOnSnapPositionChange(onChange:(position:Int) -> Unit){
        onSnapPositionChange = onChange
    }

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                snapPositionChange(recyclerView)
            }

            private fun snapPositionChange(recyclerView:RecyclerView){
                val snapPos = getSnapPosition(recyclerView)

                if(currentSnapPosition != snapPos) {
                    currentSnapPosition = snapPos

                    onSnapPositionChange(snapPos)
                }
            }
        })

        super.attachToRecyclerView(recyclerView)
    }

    companion object{
        private const val INVALID_DISTANCE = 1f
    }
}