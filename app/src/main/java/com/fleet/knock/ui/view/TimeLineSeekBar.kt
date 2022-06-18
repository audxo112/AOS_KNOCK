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
import java.lang.RuntimeException
import kotlin.math.abs
import kotlin.math.roundToInt

class TimeLineSeekBar : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private var videoUri: Uri? = null
    private var videoDuration = 0L
        set(value){
            videoMin = 0
            videoMax = if(value > 15000) value - 15000
            else 1
            field = value
        }
    private var videoMin = 0L
    private var videoMax = 0L
    private val interval: Long
        get() = (videoMax - videoMin) / timeLineMaxCount

    private var pixelRangeMin = 0.0f
    private var pixelRangeMax = 0.0f

    private var viewWidth = 0
    private var viewHeight = 0

    private var timeLineMaxCount = 1
    private var timeLineWidth = 0
    private var timeLineHeight = 1

    private val seekBar = SeekBar()

    private val seekBarOffset by lazy{
        dpToPixel(1)
    }

    private val previewList = SparseArray<TimePreview>()
    private val detector = EventDetector()

    private var loadTimeLineJobs: Array<Job?>? = null
    var listener: OnChangedListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingStart + paddingEnd + suggestedMinimumWidth
        viewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)

        val minH = paddingBottom + paddingTop + suggestedMinimumHeight
        viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)

        timeLineHeight = viewHeight - seekBarOffset * 2
        timeLineMaxCount = (viewWidth.toFloat() / (timeLineHeight * 2)).roundToInt()
        if(timeLineMaxCount <= 0)
            timeLineMaxCount = 1
        timeLineWidth = viewWidth / timeLineMaxCount

        if(timeLineMaxCount > 0 &&
            loadTimeLineJobs?.size != timeLineMaxCount)
            loadTimeLineJobs = arrayOfNulls(timeLineMaxCount)

        setMeasuredDimension(viewWidth, viewHeight)

        pixelRangeMin = dpToPixel(2.5f)
        pixelRangeMax = viewWidth - dpToPixel(2.5f)

        seekBar.onMeasure()
    }

    override fun onDraw(canvas: Canvas) {
        for(i in 0 until previewList.size)
            previewList[i]?.onDraw(canvas)

        seekBar.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!detector.obtain(event))
            return false

        when(event.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN-> listener?.onStartTrackingTouch()
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP-> listener?.onStopTrackingTouch(seekBar.value)
            MotionEvent.ACTION_MOVE ->{
                seekBar.setBarWithPos(detector.touchCurrentX)
                listener?.onSeekChanged(seekBar.value)
            }
        }
        invalidate()

        return detector.clean(event)
    }

    private fun requestLoadTimeLine(){
        if(viewWidth == 0 || viewHeight == 0){
            viewTreeObserver.addOnGlobalLayoutListener(object:ViewTreeObserver.OnGlobalLayoutListener{
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    loadTimeLine()
                }
            })
        }
        else
            loadTimeLine()
    }

    val job = Job()
    val scope = CoroutineScope(Dispatchers.IO + job)

    private fun loadTimeLine(){
        val jobs = loadTimeLineJobs ?: return
        scope.launch(Dispatchers.IO + job) {
            for(i in jobs.indices){
                jobs[i]?.cancelAndJoin()
            }

            for(i in jobs.indices){
                jobs[i] = scope.launch(Dispatchers.IO + job) {
                    var mr:MediaMetadataRetriever? = null
                    val timeLine:Bitmap?
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

                        withContext(Dispatchers.Main) {
                            invalidate()
                        }
                    }
                }
            }
        }
    }

    fun setVideo(uri:Uri, duration:Long, progress:Long = 0L){
        videoUri = uri
        videoDuration = duration

        requestVideoProgress(progress)
    }

    fun setDuration(duration:Long){
        videoDuration = duration
    }

    fun setProgress(progress:Long){
        seekBar.setBarWithValue(progress)

        requestLoadTimeLine()
    }

    private fun requestVideoProgress(progress:Long){
        if(viewWidth == 0 || viewHeight == 0 ){
            viewTreeObserver.addOnGlobalLayoutListener(object:ViewTreeObserver.OnGlobalLayoutListener{
                override fun onGlobalLayout() {
                    if(viewHeight != 0 && viewWidth != 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        setProgress(progress)
                    }
                }
            })
        }
        else
            setProgress(progress)
    }

    private fun dpToPixel(dp: Int): Int {
        val dm = Resources.getSystem().displayMetrics
        return (dp * (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun dpToPixel(dp: Float): Float {
        val dm = Resources.getSystem().displayMetrics
        return dp * (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    inner class TimePreview(index:Int, src: Bitmap){
        private var bitmap = createBitmap(index, src)
        private var rect = createRect(index)

        private fun createBitmap(index:Int, src:Bitmap) :Bitmap{
            val factor = timeLineWidth.toFloat() / timeLineHeight
            val bFactor = src.width.toFloat() / src.height

            val cropSrc = if(factor >= bFactor){
                val bh = src.height - (src.height / factor).toInt()
                Bitmap.createBitmap(src, 0, (src.height - bh) / 2, src.width, bh)
            }
            else{
                val bw = src.width - (src.width / factor).toInt()
                Bitmap.createBitmap(src, (src.width - bw) / 2, 0, bw, src.height)
            }

            val b = Bitmap.createScaledBitmap(cropSrc, timeLineWidth, timeLineHeight, false)

            if(index == 0 ||
                    index == timeLineMaxCount - 1){
                val round = dpToPixel(4).toFloat()
                val roundPath = if(index == 0)
                    floatArrayOf(round, round, 0f, 0f, 0f, 0f, round, round)
                else
                    floatArrayOf(0f, 0f, round, round, round, round, 0f, 0f)

                val output = Bitmap.createBitmap(b.width, b.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)

                val path = Path()
                val paint = Paint()
                val rect = Rect(0, 0, b.width, b.height)
                val rectF = RectF(rect)

                canvas.drawARGB(0, 0,0, 0)
                path.addRoundRect(rectF, roundPath, Path.Direction.CW)
                paint.isAntiAlias = true
                paint.color = Color.WHITE

                canvas.drawPath(path, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(b, rect, rect, paint)

                return output
            }
            else
                return b
        }

        private fun createRect(index:Int)
            = Rect(timeLineWidth * index, seekBarOffset, (index + 1) * timeLineWidth, timeLineHeight + seekBarOffset)

        fun setBitmap(index: Int, src:Bitmap){
            bitmap = createBitmap(index, src)
        }

        fun onDraw(canvas: Canvas){
            canvas.drawBitmap(bitmap, null, rect, null)
        }
    }

    inner class EventDetector{
        private var touchStartX = 0f
        private var _touchCurrentX = 0f
        val touchCurrentX
            get() = _touchCurrentX
        private var _drag = false
        val drag
            get() = _drag

        private var firstIndexId = -1
        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        fun obtain(event:MotionEvent) : Boolean{
            if(firstIndexId != -1 &&
                    firstIndexId != event.getPointerId(event.actionIndex))
                return false

            return when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN -> actionDown(event)
                MotionEvent.ACTION_MOVE -> actionMove(event)
                else -> true
            }
        }

        fun clean(event:MotionEvent) : Boolean{
            return when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL-> cleanActionUp()
                else -> true
            }
        }

        private fun actionDown(event:MotionEvent) : Boolean{
            firstIndexId = event.getPointerId(event.actionIndex)
            touchStartX = event.x
            _touchCurrentX = event.x
            return true
        }

        private fun cleanActionUp() : Boolean{
            firstIndexId = -1
            _drag = false
            return true
        }

        private fun actionMove(event:MotionEvent) : Boolean{
            for(i in 0 until event.pointerCount){
                if(firstIndexId == event.getPointerId(i)){
                    _touchCurrentX = event.getX(i)

                    if(!drag &&
                            abs(touchCurrentX - touchStartX) > touchSlop)
                        _drag = true

                    return true
                }
            }
            return false
        }
    }

    abstract inner class Bar {
        var pos = 0.0f
        var value = 0L
        var rect = Rect()

        fun setBarWithPos(p:Float){
            pos = if(p < pixelRangeMin) pixelRangeMin
            else if(p > pixelRangeMax) pixelRangeMax
            else p
            value = calculateValue()
            calculateRect()
        }

        fun setBarWithValue(v:Long){
            value = if(v < videoMin) videoMin
            else if(v > videoMax) videoMax
            else v
            pos = calculatePos()
            calculateRect()
        }

        protected fun calculateValue(): Long {
            pos = if(pos < pixelRangeMin) pixelRangeMin
            else if(pos > pixelRangeMax) pixelRangeMax
            else pos
            return ((pos / (pixelRangeMax - pixelRangeMin) * (videoMax - videoMin)) + videoMin).toLong()
        }

        protected abstract fun calculatePos(): Float

        protected abstract fun calculateRect()
    }

    inner class SeekBar : Bar(){
        val bar = resources.getDrawable(R.drawable.seek_bar, null)
        val radius by lazy{
            dpToPixel(2f).toInt()
        }

        override fun calculatePos(): Float {
            value = if(value < videoMin) videoMin
            else if(value > videoMax) videoMax
            else value
            return (value.toFloat() - videoMin) / (videoMax - videoMin) * (pixelRangeMax - pixelRangeMin) + pixelRangeMin
        }

        override fun calculateRect() {
            rect.left = pos.toInt() - radius
            rect.right = pos.toInt() + radius
        }

        fun onMeasure(){
            rect.top = 0
            rect.bottom = viewHeight

            setBarWithValue(value)
        }

        fun onDraw(canvas: Canvas){
            bar.bounds = rect
            bar.draw(canvas)
        }
    }

    interface OnChangedListener{
        fun onSeekChanged(progress:Long){}
        fun onStartTrackingTouch(){}
        fun onStopTrackingTouch(progress:Long){}
    }
}