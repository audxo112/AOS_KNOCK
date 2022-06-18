package com.fleet.knock.ui.view

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import androidx.core.util.size
import com.fleet.knock.R
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.roundToInt

class RangeTimeLineProgressBar : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private var videoUri: Uri? = null
    private var videoDuration = 0L

    var videoMin = 0L
    var videoMax = MAX_GAP
    val videoStart
        get() = thumbs[INDEX_START].value
    val videoEnd
        get() = thumbs[INDEX_END].value
    val rangeGap
        get() = thumbs[INDEX_END].value - thumbs[INDEX_START].value
    val progress
        get() = progressBar.value

    private val interval: Long
        get() = (videoMax - videoMin) / timeLineMaxCount

    private var posGap = 0.0f

    private var pixelRangeMin = 0.0f
    private var pixelRangeMax = 0.0f

    private var viewWidth = 0
    private var viewHeight = 0

    private var timeLineMaxCount = 1
    private var timeLineSize = 0

    private val thumbs = arrayOf(
        Thumb(INDEX_START),
        Thumb(INDEX_END)
    )
    private val thumbWidth = thumbs[INDEX_START].thumb.width
    private val thumbHeight = thumbs[INDEX_START].thumb.height

    private val progressBar = ProgressBar()
    private val shadow = Shadow()
    private val line = Line()

    private val previewList = SparseArray<TimePreview>()
    private val detector = EventDetector()

    private var loadTimeLineJobs: Array<Job?>? = null
    var listener: OnChangedListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingStart + paddingEnd + suggestedMinimumWidth
        viewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)

        val minH = paddingBottom + paddingTop + suggestedMinimumHeight
        viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)

        timeLineMaxCount =
            ((viewWidth.toFloat() - 2 * thumbWidth) / (viewHeight - dpToPixel(2))).roundToInt()
        if(timeLineMaxCount <= 0)
            timeLineMaxCount = 1
        timeLineSize = (viewWidth - 2 * thumbWidth) / timeLineMaxCount

        if (timeLineMaxCount > 0 &&
            loadTimeLineJobs?.size != timeLineMaxCount)
            loadTimeLineJobs = arrayOfNulls(timeLineMaxCount)

        setMeasuredDimension(viewWidth, viewHeight)

        pixelRangeMin = 0f
        pixelRangeMax = viewWidth.toFloat() - 2 * thumbWidth

        thumbs[INDEX_START].onMeasure()
        thumbs[INDEX_END].onMeasure()

        shadow.onMeasure()
        line.onMeasure()

        progressBar.onMeasure()
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until previewList.size) {
            previewList.get(i)?.onDraw(canvas)
        }
        shadow.onDraw(canvas)
        thumbs[INDEX_START].onDraw(canvas)
        thumbs[INDEX_END].onDraw(canvas)
        line.onDraw(canvas)
        progressBar.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!detector.obtain(event)) return false

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> listener?.onStartTrackingTouch()
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (!detector.isCurrentDrag()) {
                    val thumbIndex = detector.getCurrentThumbIndex()
                    if (thumbIndex == INDEX_START)
                        moveThumb(thumbIndex, progressBar.pos - thumbs[INDEX_START].pos)
                    else if (thumbIndex == INDEX_END)
                        moveThumb(thumbIndex, progressBar.pos - thumbs[INDEX_END].pos)
                    invalidate()
                }

                listener?.onStopTrackingTouch(
                    progressBar.value,
                    thumbs[INDEX_START].value,
                    thumbs[INDEX_END].value
                )
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val moveId = event.getPointerId(i)
                    if (detector.isInvalidTouch(i, moveId))
                        continue
                    if (!detector.isDragById(moveId))
                        continue

                    val thumbIndex = detector.getThumbIndex(moveId)
                    moveThumb(thumbIndex, detector.getMovement(moveId))

                    if (thumbIndex == INDEX_START ||
                        thumbIndex == INDEX_CENTER
                    ) {
                        setProgress(thumbs[INDEX_START].value, true)
                    } else if (thumbIndex == INDEX_END) {
                        setProgress(thumbs[INDEX_END].value, true)
                    }

                    listener?.onRangeChanged(thumbs[INDEX_START].value, thumbs[INDEX_END].value)
                }
            }
        }
        return detector.clean(event)
    }

    private fun moveThumb(index: Int, movement: Float) {
        val startThumb = thumbs[INDEX_START]
        val endThumb = thumbs[INDEX_END]

        var limit = posGap
        if (index == INDEX_CENTER)
            limit = endThumb.touchStartPos - startThumb.touchStartPos

        if (index == INDEX_CENTER || index == INDEX_START)
            startThumb.move(movement, endThumb.pos - limit)

        if (index == INDEX_CENTER || index == INDEX_END)
            endThumb.move(movement, startThumb.pos + limit)

        line.calculateRect()
        shadow.calculateRect()
    }

    fun setVideo(uri: Uri, duration: Long, min: Long = 0L, start: Long = -1L, end: Long = -1L) {
        videoUri = uri
        videoDuration = duration

        requestVideoMin(min, start, end)
    }

    fun setDuration(duration: Long) {
        videoDuration = duration

        if (videoMax > videoDuration) {
            videoMax = videoDuration
            thumbs[INDEX_END].value = videoDuration
        }
    }

    private fun requestVideoMin(min: Long, start: Long, end: Long) {
        if (viewHeight == 0 ||
            viewWidth == 0
        ) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if(viewHeight != 0 && viewWidth != 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        setVideoMin(min, start, end)
                    }
                }
            })
        } else setVideoMin(min, start, end)
    }

    fun setVideoMin(min: Long, start: Long = -1L, end: Long = -1L) {
        videoMin = min
        videoMax = if (videoDuration - videoMin <= MAX_GAP) videoDuration
        else videoMin + MAX_GAP

        posGap = MIN_GAP.toFloat() / (videoMax - videoMin) * (pixelRangeMax - pixelRangeMin)

        thumbs[INDEX_START].setBarWithValue(
            if (start == -1L) videoMin
            else start
        )

        thumbs[INDEX_END].setBarWithValue(
            if (end == -1L) videoMax
            else end
        )

        progressBar.setBarWithValue(
            thumbs[INDEX_START].value
        )

        shadow.calculateRect()
        line.calculateRect()

        requestLoadTimeLine()
    }

    fun setProgress(progress: Long, interactive: Boolean = false) {
        val p = if (progress < videoMin) videoMin
        else if (progress > videoMax) videoMax
        else progress

        progressBar.setBarWithValue(p)

        if (interactive)
            listener?.onProgressChanged(p)

        invalidate()
    }

    private fun dpToPixel(dp: Int): Int {
        val dm = Resources.getSystem().displayMetrics
        return (dp * (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    inner class TimePreview(index: Int, src: Bitmap) {
        var bitmap = resizeBitmap(src)

        private val rect by lazy {
            when (index) {
                0 -> RectF(
                    thumbWidth.toFloat(),
                    dpToPixel(1).toFloat(),
                    (index + 1) * timeLineSize.toFloat() + thumbWidth,
                    viewHeight.toFloat() - dpToPixel(1)
                )
                timeLineMaxCount - 1 -> RectF(
                    timeLineSize.toFloat() * index + thumbWidth,
                    dpToPixel(1).toFloat(),
                    viewWidth.toFloat() - thumbWidth,
                    viewHeight.toFloat() - dpToPixel(1)
                )
                else -> RectF(
                    timeLineSize.toFloat() * index + thumbWidth,
                    dpToPixel(1).toFloat(),
                    (index + 1) * timeLineSize.toFloat() + thumbWidth,
                    viewHeight.toFloat() - dpToPixel(1)
                )
            }
        }

        private fun resizeBitmap(src: Bitmap): Bitmap {
            val b = if (src.width > src.height) Bitmap.createBitmap(
                src,
                src.width / 2 - src.height / 2,
                0,
                src.height,
                src.height
            )
            else if (src.width < src.height) Bitmap.createBitmap(
                src,
                0,
                src.height / 2 - src.width / 2,
                src.width,
                src.width
            )
            else src
            return Bitmap.createScaledBitmap(b, timeLineSize, timeLineSize, false)
        }

        fun onDraw(canvas: Canvas) {
            canvas.drawBitmap(bitmap, null, rect, null)
        }
    }

    private fun requestLoadTimeLine() {
        if (viewHeight == 0 || viewWidth == 0) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)

                    loadTimeLine()
                }
            })
        } else loadTimeLine()
    }

    val job = Job()
    val scope = CoroutineScope(Dispatchers.IO + job)

    private fun loadTimeLine() {
        val jobs = loadTimeLineJobs ?: return
        scope.launch(Dispatchers.IO + job) {
            for(i in jobs.indices){
                jobs[i]?.cancelAndJoin()
            }

            for(i in jobs.indices){
                jobs[i] = scope.launch(Dispatchers.IO + job) {
                    var mr:MediaMetadataRetriever? = null
                    val timeLine :Bitmap?
                    try{
                        mr = MediaMetadataRetriever()
                        mr.setDataSource(context, videoUri)
                        timeLine = mr.getFrameAtTime(
                            (i * interval + videoMin) * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                    }
                    finally {
                        mr?.release()
                    }

                    timeLine?.also{
                        previewList.put(i, TimePreview(i, it))

                        withContext(Dispatchers.Main){
                            invalidate()
                        }
                    }
                }
            }
        }
    }

    inner class EventDetector {
        private val currentThumb = arrayOf(
            INDEX_NONE,
            INDEX_NONE
        )

        private val touchStartX = arrayOf(
            0.0f,
            0.0f
        )

        private val touchCurrentX = arrayOf(
            0.0f,
            0.0f
        )

        private val drag = arrayOf(
            false,
            false
        )

        private var firstIndexId = 0
        private var currentTouchIndex = 0
        private var currentTouchId = 0

        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        fun getThumbIndex(touchId: Int) = currentThumb[touchId]
        fun getCurrentThumbIndex() = getThumbIndex(currentTouchId)
        fun getMovement(id: Int) = touchCurrentX[id] - touchStartX[id]
        fun isDragById(id: Int) = drag[id]
        fun isCurrentDrag() = isDragById(currentTouchId)

        fun obtain(event: MotionEvent): Boolean {
            currentTouchIndex = event.actionIndex
            currentTouchId = event.getPointerId(currentTouchIndex)

            return when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> actionDown(event)
                MotionEvent.ACTION_POINTER_DOWN -> actionPointerDown(event)
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_POINTER_UP -> actionUp()
                MotionEvent.ACTION_MOVE -> actionMove(event)
                else -> true
            }
        }

        fun clean(event: MotionEvent): Boolean {
            return when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> cleanActionUp()
                MotionEvent.ACTION_POINTER_UP -> cleanActionPointerUp()
                else -> true
            }
        }

        private fun getClosetThumb(x: Float, y: Float): Int {
            val start = thumbs[INDEX_START]
            val end = thumbs[INDEX_END]

            val centerLength = end.rect.left.toFloat() - start.rect.right
            val center = start.rect.right + centerLength / 2
            var centerStart = start.rect.right.toFloat()
            var centerEnd = end.rect.left.toFloat()

            if (centerLength <= thumbHeight) {
                centerStart = center - thumbWidth.toFloat() / 2
                centerEnd = center + thumbWidth.toFloat() / 2
            }

            return if (x in centerStart..centerEnd) INDEX_CENTER
            else if (start.contains(x, y)) INDEX_START
            else if (end.contains(x, y)) INDEX_END
            else INDEX_NONE
        }

        private fun startTouchThumb(thumbIndex: Int) {
            if (thumbIndex == INDEX_NONE) return

            if (thumbIndex == INDEX_CENTER) {
                thumbs[INDEX_START].touchStartPos = thumbs[INDEX_START].pos
                thumbs[INDEX_END].touchStartPos = thumbs[INDEX_END].pos
            } else {
                thumbs[thumbIndex].touchStartPos = thumbs[thumbIndex].pos
            }
        }

        fun isInvalidTouch(index: Int, id: Int): Boolean {
            return index >= currentThumb.size || currentThumb[id] == INDEX_NONE
        }

        private fun actionDown(event: MotionEvent): Boolean {
            firstIndexId = event.getPointerId(event.actionIndex)

            currentThumb[firstIndexId] = getClosetThumb(event.x, event.y)
            if (currentThumb[firstIndexId] == INDEX_NONE)
                return false
            touchStartX[firstIndexId] = event.x
            touchCurrentX[firstIndexId] = event.x

            startTouchThumb(currentThumb[firstIndexId])
            return true
        }

        private fun actionPointerDown(event: MotionEvent): Boolean {
            if (currentTouchIndex >= currentThumb.size ||
                currentThumb[firstIndexId] == INDEX_CENTER
            )
                return false

            currentThumb[currentTouchId] =
                getClosetThumb(event.getX(currentTouchIndex), event.getY(currentTouchIndex))
            if (currentThumb[currentTouchId] == currentThumb[firstIndexId] ||
                currentThumb[currentTouchId] == INDEX_NONE ||
                currentThumb[currentTouchId] == INDEX_CENTER
            ) {
                currentThumb[currentTouchId] = INDEX_NONE
                return false
            }
            touchStartX[currentTouchId] = event.getX(currentTouchIndex)
            touchCurrentX[currentTouchId] = event.getX(currentTouchIndex)

            startTouchThumb(currentThumb[currentTouchId])

            return true
        }

        private fun actionUp(): Boolean {
            return !isInvalidTouch(currentTouchIndex, currentTouchId)
        }

        private fun cleanActionUp(): Boolean {
            currentThumb[currentTouchId] = INDEX_NONE
            drag[currentTouchId] = false
            return true
        }

        private fun cleanActionPointerUp(): Boolean {
            if (firstIndexId == currentTouchId)
                firstIndexId = if (currentTouchId == 1) 0
                else 1
            return true
        }

        private fun actionMove(event: MotionEvent): Boolean {
            if (currentThumb[0] == INDEX_NONE && currentThumb[1] == INDEX_NONE)
                return false

            for (i in 0 until event.pointerCount) {
                val moveId = event.getPointerId(i)
                if (isInvalidTouch(i, moveId))
                    continue
                touchCurrentX[moveId] = event.getX(i)
                if (!drag[moveId] && abs(touchStartX[moveId] - touchCurrentX[moveId]) > touchSlop)
                    drag[moveId] = true
            }
            return true
        }
    }

    abstract inner class Bar {
        var touchStartPos = 0.0f
        var pos = 0.0f
        var value = 0L
        var rect = Rect()

        fun setBarWithValue(v: Long) {
            value = v
            pos = calculatePos()
            touchStartPos = pos
            calculateRect()
        }

        protected fun calculateValue(): Long {
            pos = if (pos < pixelRangeMin) pixelRangeMin
            else if (pos > pixelRangeMax) pixelRangeMax
            else pos
            return ((pos / (pixelRangeMax - pixelRangeMin) * (videoMax - videoMin)) + videoMin).toLong()
        }

        protected abstract fun calculatePos(): Float

        protected abstract fun calculateRect()
    }

    inner class ProgressBar : Bar() {
        private val bar = resources.getDrawable(R.drawable.progress_bar, null)

        override fun calculatePos(): Float {
            value = if (value < thumbs[INDEX_START].value) thumbs[INDEX_START].value
            else if (value > thumbs[INDEX_END].value) thumbs[INDEX_END].value
            else value
            return (value - thumbs[INDEX_START].value).toFloat() / (thumbs[INDEX_END].value - thumbs[INDEX_START].value) * (thumbs[INDEX_END].pos - thumbs[INDEX_START].pos) + thumbs[INDEX_START].pos
        }

        override fun calculateRect() {
            rect.left = pos.toInt() - dpToPixel(2) + thumbWidth
            rect.right = pos.toInt() + dpToPixel(2) + thumbWidth
        }

        fun onMeasure() {
            rect.top = 0
            rect.bottom = viewHeight

            calculateRect()
        }

        fun onDraw(canvas: Canvas) {
            bar.bounds = rect
            bar.draw(canvas)
        }
    }

    inner class Thumb(private val index: Int) : Bar() {
        val thumb: Bitmap = if (index == INDEX_START) BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_editor_start
        )
        else BitmapFactory.decodeResource(resources, R.drawable.ic_editor_end)

        init {
            value = if (index == INDEX_START) 0
            else MAX_GAP
        }

        override fun calculatePos(): Float {
            value = if (value < videoMin) videoMin
            else if (value > videoMax) videoMax
            else value
            return (value - videoMin).toFloat() / (videoMax - videoMin) * (pixelRangeMax - pixelRangeMin)
        }

        override fun calculateRect() {
            if (index == INDEX_START) {
                rect.left = pos.toInt()
                rect.right = pos.toInt() + thumb.width
            } else {
                rect.left = pos.toInt() + thumb.width
                rect.right = pos.toInt() + 2 * thumb.width
            }
        }

        fun move(move: Float, limit: Float) {
            pos = if (index == INDEX_START) {
                if (touchStartPos + move < pixelRangeMin) pixelRangeMin
                else if (touchStartPos + move > limit) limit
                else touchStartPos + move
            } else {
                if (touchStartPos + move > pixelRangeMax) pixelRangeMax
                else if (touchStartPos + move < limit) limit
                else touchStartPos + move
            }
            value = calculateValue()
            calculateRect()
        }

        fun onMeasure() {
            rect.top = dpToPixel(1)
            rect.bottom = viewHeight - dpToPixel(1)

            setBarWithValue(value)
        }

        fun onDraw(canvas: Canvas) {
            canvas.drawBitmap(thumb, null, rect, null)
        }

        fun contains(x: Float, y: Float) = rect.contains(x.toInt(), y.toInt())
    }

    inner class Line {
        private val topRect = Rect()
        private val bottomRect = Rect()
        private val paint = Paint().apply {
            color = -0x207901
        }

        fun calculateRect() {
            topRect.left = thumbs[INDEX_START].rect.right
            topRect.right = thumbs[INDEX_END].rect.left

            bottomRect.left = thumbs[INDEX_START].rect.right
            bottomRect.right = thumbs[INDEX_END].rect.left
        }

        fun onDraw(canvas: Canvas) {
            canvas.drawRect(topRect, paint)
            canvas.drawRect(bottomRect, paint)
        }

        fun onMeasure() {
            topRect.top = dpToPixel(1)
            topRect.bottom = dpToPixel(4)

            bottomRect.top = viewHeight - dpToPixel(4)
            bottomRect.bottom = viewHeight - dpToPixel(1)

            calculateRect()
        }
    }

    inner class Shadow {
        private val startThumbShadow =
            resources.getDrawable(R.drawable.thumb_range_start_shadow, null)
        private val endThumbShadow = resources.getDrawable(R.drawable.thumb_range_end_shadow, null)
        private val startThumbShadowRect = Rect()
        private val endThumbShadowRect = Rect()

        private val paint = Paint().apply {
            color = -0x80000000
        }

        fun calculateRect() {
            startThumbShadowRect.right = thumbs[INDEX_START].rect.right
            endThumbShadowRect.left = thumbs[INDEX_END].rect.left
        }

        fun onDraw(canvas: Canvas) {
            startThumbShadow.bounds = startThumbShadowRect
            startThumbShadow.draw(canvas)

            endThumbShadow.bounds = endThumbShadowRect
            endThumbShadow.draw(canvas)
        }

        fun onMeasure() {
            startThumbShadowRect.apply {
                top = dpToPixel(1)
                bottom = viewHeight - dpToPixel(1)
                left = 0
                right = thumbWidth
            }

            endThumbShadowRect.apply {
                top = dpToPixel(1)
                bottom = viewHeight - dpToPixel(1)
                left = viewWidth - thumbWidth
                right = viewWidth
            }
            calculateRect()
        }
    }

    interface OnChangedListener {
        fun onProgressChanged(progress: Long) {}
        fun onRangeChanged(start: Long, end: Long) {}
        fun onStartTrackingTouch() {}
        fun onStopTrackingTouch(progress: Long, start: Long, end: Long) {}
    }

    companion object {
        private const val INDEX_NONE = -1
        private const val INDEX_START = 0
        private const val INDEX_END = 1
        private const val INDEX_CENTER = 2

        private const val MAX_GAP = 30000L
        private const val MIN_GAP = 1000L
    }
}