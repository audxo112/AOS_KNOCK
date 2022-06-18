package com.fleet.knock.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.video.VideoListener
import pl.droidsonroids.gif.GifImageView
import java.io.File
import kotlin.math.abs

open class FPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? =  /* attrs= */null,
    defStyleAttr: Int =  /* defStyleAttr= */0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val componentListener = ComponentListener()

    protected var shutterView: View?

    protected var frameContainer:FrameLayout? = null
    protected var contentFrameBackground:View? = null
    var contentFrame: AspectRatioLayout? = null
    protected var contentLine:View? = null
    var videoSurfaceView: View? = null

    protected var overlayFrameLayout: ConstraintLayout?
    protected var overlayFrame: ImageView? = null
    open var player: Player? = null
        set(value){
            Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
            Assertions.checkArgument(
                value == null || value.applicationLooper == Looper.getMainLooper()
            )
            if (field === value) {
                return
            }
            val oldPlayer = field
            if (oldPlayer != null) {
                oldPlayer.removeListener(componentListener)
                val oldVideoComponent = oldPlayer.videoComponent
                if (oldVideoComponent != null) {
                    oldVideoComponent.removeVideoListener(componentListener)
                    oldVideoComponent.clearVideoTextureView(videoSurfaceView as TextureView?)
                }
            }
            field = value
            if (value != null) {
                val newVideoComponent = value.videoComponent
                newVideoComponent?.setVideoTextureView(videoSurfaceView as TextureView?)
                newVideoComponent?.addVideoListener(componentListener)

                value.addListener(componentListener)
            }
        }

    private var textureViewRotation = 0

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        videoSurfaceView?.visibility = visibility
    }

    var resizeMode: Int
        get() = contentFrame?.resizeMode ?: AspectRatioLayout.RESIZE_MODE_FILL
        set(resizeMode) {
            contentFrame?.resizeMode = resizeMode
        }

    var shutterColor:Int = Color.BLACK

    var videoRotation:Float
        get() = contentFrame?.rotation ?: 0.0f
        set(rotation){
            contentFrame?.rotation = rotation
        }

    var videoRotationY:Float
        get() = contentFrame?.rotationY ?: 0.0f
        set(rotation){
            contentFrame?.rotationY = rotation
        }

    open var mode = MODE_RESIZE
    private var eventMode = EVENT_MODE_DRAG

    private val seekDetector = SeekDetector()
    private val doubleTapDetector = DoubleTapDetector()
    private val scaleDetector = ScaleDetector()
    private val dragDetector = DragDetector()

    protected open val minProgress
        get() = 0L

    protected open val maxProgress
        get() = player?.duration ?: 1L

    var preventEvent = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if(preventEvent)
            return false

        return when(mode){
            MODE_PLAY -> playModeOnTouchEvent(event)
            MODE_RESIZE -> resizeModeOnTouchEvent(event)
            else -> false
        }
    }

    private fun playModeOnTouchEvent(event:MotionEvent) : Boolean{
        seekDetector.onTouchEvent(event)

        return true
    }

    private fun resizeModeOnTouchEvent(event:MotionEvent) : Boolean{
        val handled = doubleTapDetector.onTouchEvent(event)
        if(handled)
            return false

        scaleDetector.onTouchEvent(event)

        if(eventMode == EVENT_MODE_ZOOM) {
            doubleTapDetector.interrupt = true
            return true
        }

        dragDetector.onTouchEvent(event)

        return true
    }

    fun setShutterBackgroundColor(color: Int) {
        shutterColor = color
        contentFrameBackground?.setBackgroundColor(
            if(color == Color.BLACK) ContextCompat.getColor(context, R.color.colorBackgroundBrightGrayA15)
            else ContextCompat.getColor(context, R.color.colorBackgroundBlackA10)
        )

        shutterView?.setBackgroundColor(color)
    }

    fun setFrameDrawable(drawable: Drawable?, scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP){
        overlayFrame?.setImageDrawable(drawable)
        overlayFrame?.scaleType = scaleType
    }

    fun setFrameImage(file:File?, scaleType: ImageView.ScaleType){
        val img = overlayFrame?:return
        img.scaleType = scaleType
        val builder = Glide.with(this).load(file)

        if(scaleType == ImageView.ScaleType.CENTER_CROP)
            builder.centerCrop()
        else
            builder.fitCenter()

        builder.into(img)
    }

    fun setFrameGif(file:File?, scaleType: ImageView.ScaleType){
        val img = overlayFrame?:return
        img.scaleType = scaleType
        val builder = Glide.with(this).asGif().load(file)

        if(scaleType == ImageView.ScaleType.CENTER_CROP)
            builder.centerCrop()
        else
            builder.fitCenter()

        builder.into(img)
    }

    fun setFrameLayout(ratio:String = "",
                      startMargin:Int = 0,
                      topMargin:Int = 0,
                      endMargin:Int = 0,
                      bottomMargin:Int = 0,
                      verticalBias:Float = 0.5f,
                      horizontalBias:Float = 0.5f){
        contentFrameBackground?.visibility =
            if(ratio == "") View.INVISIBLE
            else View.VISIBLE
        val frameContainer = frameContainer ?: return
        val set = ConstraintSet().apply{
            clone(this@FPlayerView)
            setDimensionRatio(frameContainer.id, ratio)
            setMargin(frameContainer.id, ConstraintSet.START, startMargin)
            setMargin(frameContainer.id, ConstraintSet.TOP, topMargin)
            setMargin(frameContainer.id, ConstraintSet.END, endMargin)
            setMargin(frameContainer.id, ConstraintSet.BOTTOM, bottomMargin)

            setVerticalBias(frameContainer.id, verticalBias)
            setHorizontalBias(frameContainer.id, horizontalBias)
        }
        set.applyTo(this@FPlayerView)
    }

    private fun applyTextureViewRotation(
        textureView: TextureView,
        textureViewRotation: Int
    ) {
        val transformMatrix = Matrix()
        val textureViewWidth = textureView.width.toFloat()
        val textureViewHeight = textureView.height.toFloat()

        if (textureViewWidth != 0f && textureViewHeight != 0f && textureViewRotation != 0) {
            val pivotX = textureViewWidth / 2
            val pivotY = textureViewHeight / 2
            transformMatrix.postRotate(textureViewRotation.toFloat(), pivotX, pivotY)

            // After rotation, scale the rotated texture to fit the TextureView size.
            val originalTextureRect = RectF(0f, 0f, textureViewWidth, textureViewHeight)
            val rotatedTextureRect = RectF()

            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect)
            transformMatrix.postScale(
                textureViewWidth / rotatedTextureRect.width(),
                textureViewHeight / rotatedTextureRect.height(),
                pivotX,
                pivotY
            )
        }
        textureView.setTransform(transformMatrix)
    }

    protected open var themeUri:Uri? = null

    fun setVideo(uri:Uri?){
        uri ?: return
        themeUri = uri
        loadVideo(uri)
    }

    protected fun loadVideo(uri: Uri){
        val source = FileDataSource().let{ dataSource->
            dataSource.open(DataSpec(uri))
            DataSource.Factory { dataSource }.let{ factory->
                ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
            }
        } ?: return

        loadVideo(source)
    }

    protected fun loadVideo(source: ProgressiveMediaSource){
        val sp = player as SimpleExoPlayer? ?: return

        sp.run{
            val backupPlayWhenReady = playWhenReady
            playWhenReady = false
            stop()
            prepare(source)
            playWhenReady = backupPlayWhenReady
        }
    }

    protected fun seekBegin(){
        player?.playWhenReady = false
        if(player is SimpleExoPlayer)
            (player as SimpleExoPlayer).setSeekParameters(SeekParameters.CLOSEST_SYNC)
    }

    protected fun seekEnd(progress:Long){
        if(player is SimpleExoPlayer)
            (player as SimpleExoPlayer).setSeekParameters(SeekParameters.EXACT)
        player?.seekTo(progress)
    }

    protected open fun onSeek(progress:Long) {
        val p = player as SimpleExoPlayer
        if(progress <= minProgress || progress >= maxProgress){
            p.setSeekParameters(SeekParameters.EXACT)
        }
        else {
            p.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
        p.seekTo(progress)
    }

    private inner class SeekDetector{
        private var touchStartX = 0f
        private var startProgress = 0L
        private var speed = 0L

        private fun seekInit(event:MotionEvent){
            touchStartX = event.x
            startProgress = player?.currentPosition ?: 0L
            speed = if((maxProgress - minProgress) / 750 > 5)
                (maxProgress - minProgress) / 750
            else 5
        }

        private fun getProgress(event:MotionEvent) : Long{
            val progress = startProgress + ((event.x - touchStartX) * speed).toLong()
            return if(progress < minProgress){
                touchStartX = event.x
                startProgress = minProgress
                minProgress
            }
            else if(progress > maxProgress){
                touchStartX = event.x
                startProgress = maxProgress
                maxProgress
            }
            else progress
        }

        fun onTouchEvent(event:MotionEvent){
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    seekInit(event)
                    seekBegin()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL->{
                    seekEnd(getProgress(event))
                }
                MotionEvent.ACTION_MOVE->{
                    onSeek(getProgress(event))
                }
            }
        }
    }

    private inner class DoubleTapDetector{
        private var firstDown = PointF()
        private var firstUpTime = 0L

        var interrupt = false
        private var isDoubleTap = false

        private val tapTimeOut = ViewConfiguration.getDoubleTapTimeout()
        private val tapMinTime = 40
        private val slopSquare = 50f

        fun onTouchEvent(event:MotionEvent) : Boolean{
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN -> {
                    if(interrupt)
                        return false

                    isDoubleTap = isConsideredDoubleTap(firstDown, event)
                    if(isDoubleTap)
                        firstDown.set(-1f, -1f)
                    else
                        firstDown.set(event.x, event.y)

                    if(isDoubleTap) {
                        onDoubleTap()
                        init()
                    }
                }
                MotionEvent.ACTION_UP ->{
                    if(interrupt)
                        init()
                    interrupt = false

                    firstUpTime = if(isDoubleTap) 0L
                    else System.currentTimeMillis()
                }
            }

            return isDoubleTap
        }

        private fun init(){
            firstDown.set(-1f, -1f)
            firstUpTime = 0L
        }

        private fun isConsideredDoubleTap(firstDown:PointF, currentDown:MotionEvent) : Boolean{
            if(firstDown.x == -1f || firstDown.y == -1f)
                return false

            val deltaTime = System.currentTimeMillis() - firstUpTime
            if(deltaTime > tapTimeOut || deltaTime < tapMinTime)
                return false

            val deltaX = abs(currentDown.x - firstDown.x)
            val deltaY = abs(currentDown.y - firstDown.y)

            return deltaX < slopSquare && deltaY < slopSquare
        }

        private fun onDoubleTap(){
            resizeMode = resizeMode
        }
    }

    private inner class ScaleDetector : ScaleGestureDetector(context, object:OnScaleGestureListener{
        var originScale = 1f
        var maxScale = 5.0f
        var minScale = 0.3f

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            eventMode = EVENT_MODE_ZOOM
            originScale = contentFrame?.scaleX ?: 1f
            overlayFrameLayout?.alpha = 0.7f
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            eventMode = EVENT_MODE_NONE
            overlayFrameLayout?.alpha = 1f
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            detector?:return false

            originScale *= detector.scaleFactor
            if(originScale >= maxScale)
                originScale = maxScale
            else if(originScale <= minScale)
                originScale = minScale

            contentFrame?.scaleX = originScale
            contentFrame?.scaleY = originScale

            return true
        }
    })

    private inner class DragDetector{
        private val dragStart = PointF()
        private val dragBase = PointF()

        private fun dragBegin(event:MotionEvent){
            dragStart.set(event.x, event.y)
            dragBase.set(contentFrame?.translationX ?: 0F, contentFrame?.translationY ?: 0f)
            overlayFrameLayout?.alpha = 0.7f
        }

        private fun dragEnd(){
            eventMode = EVENT_MODE_DRAG
            overlayFrameLayout?.alpha = 1f
        }

        private fun onDrag(event:MotionEvent){
            val deltaX = event.x - dragStart.x
            val deltaY = event.y - dragStart.y

            contentFrame?.translationX = deltaX + dragBase.x
            contentFrame?.translationY = deltaY + dragBase.y

            if(abs(deltaX) + abs(deltaY) > 40)
                doubleTapDetector.interrupt = true
        }

        fun onTouchEvent(event:MotionEvent){
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN ->{
                    dragBegin(event)
                }
                MotionEvent.ACTION_UP ->{
                    dragEnd()
                }
                MotionEvent.ACTION_MOVE ->{
                    if(eventMode == EVENT_MODE_DRAG) {
                        onDrag(event)
                    }
                }
            }
        }
    }

    private inner class ComponentListener : Player.EventListener,
        VideoListener, OnLayoutChangeListener {

        override fun onVideoSizeChanged(
            width: Int,
            height: Int,
            unappliedRotationDegrees: Int,
            pixelWidthHeightRatio: Float
        ) {
            var videoAspectRatio: Float =
                if (height == 0 || width == 0) 1f
                else width.toFloat() / height
//                else width * pixelWidthHeightRatio / height

            if (videoSurfaceView is TextureView) {
                if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
                    videoAspectRatio = 1 / videoAspectRatio
                }
                if (textureViewRotation != 0) {
                    videoSurfaceView?.removeOnLayoutChangeListener(this)
                }
                textureViewRotation = unappliedRotationDegrees
                if (textureViewRotation != 0) {
                    videoSurfaceView?.addOnLayoutChangeListener(this)
                }
                applyTextureViewRotation(
                    videoSurfaceView as TextureView,
                    textureViewRotation
                )
            }
            contentFrame?.videoAspectRatio = videoAspectRatio
        }

        override fun onRenderedFirstFrame() {
            shutterView?.visibility = View.VISIBLE
        }

        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            applyTextureViewRotation(view as TextureView, textureViewRotation)
        }
    }

    init {
        if (isInEditMode) {
            contentFrame = null
            shutterView = null
            videoSurfaceView = null
            overlayFrameLayout = null
        }
        else {
            var resizeMode = AspectRatioLayout.RESIZE_MODE_FIT
            if (attrs != null) {
//                val a =
//                    context.theme.obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0)
//                try {
//                    shutterColor =
//                        a.getColor(R.styleable.PlayerView_shutter_background_color, shutterColor)
//                    resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, resizeMode)
//                } finally {
//                    a.recycle()
//                }
            }
            LayoutInflater.from(context).inflate(R.layout.view_fplayer, this)
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

            contentLine = findViewById(R.id.content_line)
            contentLine?.background = background

            frameContainer = findViewById(R.id.frame_container)
            contentFrameBackground = findViewById(R.id.exo_content_frame_background)

            contentFrame = findViewById(R.id.exo_content_frame)
            contentFrame?.resizeMode = resizeMode

            shutterView = findViewById(R.id.exo_shutter)
//            shutterView?.setBackgroundColor(shutterColor)

            if (contentFrame != null) {
                videoSurfaceView = TextureView(context).apply{
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                contentFrame?.addView(videoSurfaceView, 0)
            } else {
                videoSurfaceView = null
            }

            overlayFrameLayout = findViewById(R.id.exo_overlay)
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
                overlayFrame = ImageView(context)
            }
            else{
                overlayFrame = GifImageView(context)
            }
            overlayFrame?.apply{
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            overlayFrameLayout?.addView(overlayFrame, 0)
        }
    }

    companion object {
        const val MODE_PLAY = 0
        const val MODE_RESIZE = 1

        private const val EVENT_MODE_NONE = 0
        private const val EVENT_MODE_ZOOM = 1
        private const val EVENT_MODE_DRAG = 2
    }
}