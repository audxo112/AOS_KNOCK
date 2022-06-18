package com.fleet.knock.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import com.arthenica.mobileffmpeg.FFprobe
import com.fleet.knock.R
import com.fleet.knock.info.editor.BaseThemeFrame
import com.fleet.knock.info.editor.ThemeFrame
import com.fleet.knock.utils.UriUtil
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.net.URI

class EditorPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? =  /* attrs= */null,
    defStyleAttr: Int =  /* defStyleAttr= */0
) : FPlayerView(context, attrs, defStyleAttr){

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    var progressBar:RangeTimeLineProgressBar? = null
        set(value){
            field?.listener = null
            value?.listener = trimListener
            field = value
        }

    var seekBar:TimeLineSeekBar? = null
        set(value){
            field?.listener = null
            value?.listener = trimListener
            field = value
        }

    private val trimListener = TrimListener()

    override var player: Player? = null
        set(value) {
            super.player = value
            field?.removeListener(playerListener)
            value?.addListener(playerListener)
            field = value
        }

    private val playerListener = PlayerListener()

    override var mode = MODE_RESIZE
        set(value) {
            val playing = player?.playWhenReady ?: false
            updateProgress(playing && (value == MODE_PLAY || value == MODE_RESIZE))
            field = value
        }

    private var updateProgressJob:Job? = null
    private fun updateProgress(run:Boolean){
        if(run){
            if(updateProgressJob?.isActive == true)
                return

            updateProgressJob = scope.launch{
                while(isActive){
                    var pos = player?.currentPosition ?: 0L
                    if(pos < minProgress || pos >= maxProgress ||
                            player?.playbackState == Player.STATE_ENDED){
                        pos = minProgress
                        player?.seekTo(pos)
                    }
                    if(mode == MODE_PLAY)
                        progressBar?.setProgress(pos)
                    delay(20)
                }
            }
        }
        else{
            updateProgressJob?.cancel()
        }
    }

    private val filterComplexMaker = FilterComplexMaker()

    override val minProgress: Long
        get() = progressBar?.videoStart ?: super.minProgress
    val minTime : Float
        get() = minProgress.toFloat() / 1000

    override val maxProgress: Long
        get() = progressBar?.videoEnd ?: super.maxProgress
    val maxTime : Float
        get() = maxProgress.toFloat() / 1000

    val totalTime
        get() = (maxProgress - minProgress).toFloat() / 1000

    override var themeUri: Uri? = null
        set(value) {
            scope.launch(Dispatchers.Default){
                frameRate = calculateFrameRate(value)
            }
            field = value
        }

    var frameRate:Float = 0f
        private set

    fun calculateProgress(frameNumber:Int, boomerang: Boolean) =
        (frameNumber * 100 / (frameRate * totalTime * if(boomerang) 2 else 1)).toInt()

    private fun calculateFrameRate(uri:Uri?) : Float{
        uri ?: return 30.0f
        val path = UriUtil.getPath(context, uri)

        return FFprobe.getMediaInformation(path).let{
            val stream = when{
                it?.streams?.get(0)?.type == "video" -> it.streams[0]
                it?.streams?.get(1)?.type == "video" -> it.streams[1]
                else -> null
            }

            if(stream == null)
                30.0f
            else{
                try{
                    stream.realFrameRate?.toFloat() ?: 30.0f
                }
                catch (e: NumberFormatException){
                    stream.averageFrameRate?.filter { c->
                        c == '.' || c.isDigit()
                    }?.toFloat() ?: 30.0f
                }
                catch (e:Exception){
                    30.0f
                }
            }
        }
    }

    private val bSetting by lazy {
        EditorBackUpSetting()
    }

    override fun onSeek(progress: Long) {
        super.onSeek(progress)

        progressBar?.setProgress(progress)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(preventEvent || mode == MODE_NONE)
            return false

        return super.onTouchEvent(event)
    }

    var baseFrame:BaseThemeFrame? = null
        private set
    var templateFrame:ThemeFrame? = null
        private set

    fun setFrameTemplate(template:ThemeFrame?){
        baseFrame = null
        templateFrame = template

        setFrameLayout()

        val scaleType = if(template?.scaleType == ThemeFrame.SCALE_TYPE_CROP)
            ImageView.ScaleType.CENTER_CROP
        else ImageView.ScaleType.FIT_XY

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            when(template?.type) {
                ThemeFrame.TYPE_GIF -> setFrameGif(template.getFrame(context).file, scaleType)
                ThemeFrame.TYPE_IMAGE -> setFrameImage(template.getFrame(context).file, scaleType)
                else -> setFrameDrawable(null)
            }
        }
        else{
            setFrameDrawable(when(template?.type){
                ThemeFrame.TYPE_GIF -> template.getFrame(context).gif
                ThemeFrame.TYPE_IMAGE -> template.getFrame(context).image
                else -> null
            }, scaleType)
        }
    }

    fun setFrameBase(base: BaseThemeFrame?){
        baseFrame = base
        templateFrame = null

        setFrameDrawable(null)
        if(base == null)
            setFrameLayout()
        else{
            setFrameLayout(
                base.ratio,
                base.startMargin(width),
                base.topMargin(height),
                base.endMargin(width),
                base.bottomMargin(height),
                base.verticalBias,
                base.horizontalBias
            )
        }
    }

    fun makeFilterComplex(size:Point, boomerang: Boolean) = filterComplexMaker.makeFilterComplex(size, boomerang)

    private inner class TrimListener : RangeTimeLineProgressBar.OnChangedListener, TimeLineSeekBar.OnChangedListener{
        override fun onProgressChanged(progress: Long) {
            player?.seekTo(progress)
        }

        override fun onSeekChanged(progress: Long) {
            player?.seekTo(progress)
        }

        override fun onStartTrackingTouch() {
            seekBegin()
        }

        override fun onStopTrackingTouch(progress: Long, start: Long, end: Long) {
            seekEnd(progress)
        }

        override fun onStopTrackingTouch(progress: Long) {
            progressBar?.setVideoMin(progress)
            seekEnd(progress)
        }
    }

    private inner class PlayerListener : Player.EventListener{
        var themeInit = false

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if(!themeInit){
                val duration = player?.duration ?: 0
                val uri = themeUri
                if(uri != null && duration > 0 && playbackState != Player.STATE_BUFFERING) {
                    themeInit = true
                    progressBar?.setVideo(uri, duration)
                    seekBar?.setVideo(uri, duration)
                }
            }
            if(mode == MODE_PLAY) {
                updateProgress(playWhenReady && mode == MODE_PLAY)
            }
        }
    }

    fun encodingLog() : Bundle{
        val isCut = minProgress != 0L || maxProgress - minProgress != player?.duration
        return Bundle().apply{
            putBoolean("자르기", isCut)
            putLong("편집영상길이", maxProgress - minProgress)
            putLong("원본영상길이", player?.duration ?: 0L)
            putString("프레임_종류",
                if(baseFrame != null) baseFrame?.title ?: ""
                else if(templateFrame != null) templateFrame?.title ?: ""
                else "기본")
            putString("배경색", String.format("#%06X", 0xFFFFFF and shutterColor))
        }
    }

    fun startClear(){
        bSetting.startClear()
    }

    fun endClear(){
        bSetting.endClear()
    }

    fun startRestore(){
        bSetting.startRestore()
    }

    fun endRestore(){
        bSetting.endRestore()
    }

    private inner class EditorBackUpSetting{
        private var scaleX = 1f
        private var scaleY = 1f

        private var translationX = 0f
        private var translationY = 0f

        private var paddingStart = 0
        private var paddingTop = 0
        private var paddingEnd = 0
        private var paddingBottom = 0

        private var background:Drawable? = null

        private var shutterColor:Int = Color.BLACK

        private var baseFrame:BaseThemeFrame? = null
        private var templateFrame:ThemeFrame? = null

        init{
            paddingStart = this@EditorPlayerView.paddingStart
            paddingTop = this@EditorPlayerView.paddingTop
            paddingEnd = this@EditorPlayerView.paddingEnd
            paddingBottom = this@EditorPlayerView.paddingBottom

            background = this@EditorPlayerView.background
        }

        private fun backUp(){
            shutterColor = this@EditorPlayerView.shutterColor

            baseFrame = this@EditorPlayerView.baseFrame
            templateFrame = this@EditorPlayerView.templateFrame

            scaleX = contentFrame?.scaleX ?: 1f
            scaleY = contentFrame?.scaleY ?: 1f

            translationX = contentFrame?.translationX ?: 0f
            translationY = contentFrame?.translationY ?: 0f
        }

        fun startClear(){
            backUp()

            this@EditorPlayerView.setPadding(0, 0, 0, 0)

            contentFrame?.scaleX = 1f
            contentFrame?.scaleY = 1f

            contentFrame?.translationX = 0f
            contentFrame?.translationY = 0f

            setShutterBackgroundColor(Color.BLACK)

            if(baseFrame != null)
                setFrameBase(null)
            else if(templateFrame != null)
                setFrameTemplate(null)

            contentFrame?.visibility = View.INVISIBLE
            player?.playWhenReady = false
        }

        fun endClear(){
            this@EditorPlayerView.background = null
            contentLine?.visibility = View.INVISIBLE

            contentFrame?.visibility = View.VISIBLE
            player?.seekTo(0)
            player?.playWhenReady = true
        }

        fun startRestore(){
            this@EditorPlayerView.setPadding(
                paddingStart,
                paddingTop,
                paddingEnd,
                paddingBottom
            )

            contentFrame?.scaleX = scaleX
            contentFrame?.scaleY = scaleY

            contentFrame?.translationX = translationX
            contentFrame?.translationY = translationY

            this@EditorPlayerView.background = background
            contentLine?.visibility = View.VISIBLE

            if(baseFrame != null)
                setFrameBase(baseFrame)
            else if(templateFrame != null)
                setFrameTemplate(templateFrame)

            contentFrame?.visibility = View.INVISIBLE
            player?.playWhenReady = false
        }

        fun endRestore(){
            setShutterBackgroundColor(shutterColor)

            contentFrame?.visibility = View.VISIBLE
            player?.seekTo(minProgress)
            player?.playWhenReady = true
        }
    }

    private inner class FilterComplexMaker{
        private val baseLoc by lazy {
            intArrayOf(0, 0).also {
                getLocationInWindow(it)
            }
        }

        private fun getColorCode(color:Int) : String{
            return String.format("%06X", (0xFFFFFF and color))
        }

        private fun getRect(view: View?, factor:Float = 1f) : Rect {
            view ?: return Rect()
            return Rect().apply {
                val loc = intArrayOf(0, 0).also { arr ->
                    view.getLocationInWindow(arr)
                }
                set(((loc[0] - baseLoc[0] + 0.01f) * factor).toInt(),
                    ((loc[1] - baseLoc[1] + 0.01f) * factor).toInt(),
                    ((loc[0] - baseLoc[0] + view.width * view.scaleX + 0.01f) * factor).toInt(),
                    ((loc[1] - baseLoc[1] + view.height * view.scaleY + 0.01f) * factor).toInt())
            }
        }

        private fun intersection(r1: Rect, r2: Rect) : Rect {
            return Rect(
                if(r1.left >= r2.left) r1.left else r2.left,
                if(r1.top >= r2.top) r1.top else r2.top,
                if(r1.right <= r2.right) r1.right else r2.right,
                if(r1.bottom <= r2.bottom) r1.bottom else r2.bottom
            )
        }

        fun makeFilterComplex(screenSize: Point, boomerang:Boolean) : String{
            val command = StringBuilder()

            val cFrame = contentFrame ?: return ""

            val factor = screenSize.x.toFloat() / width * 0.75f

            val size = Point(screenSize).apply {
                x = x * 3 / 4 - (x * 3 / 4) % 2
                y = y * 3 / 4 - (y * 3 / 4) % 2
            }

            val fCRect = getRect(frameContainer, factor)
            val fRect = getRect(contentFrame, factor)

            command.append("-filter_complex \"color=${getColorCode(shutterColor)}:s=${size.x}x${size.y} [bg];")

            val isCut = minProgress != 0L || maxProgress - minProgress != player?.duration
            if(isCut)
                command.append("[0:v] trim=start=${minTime}:duration=${maxTime-minTime},setpts=PTS-STARTPTS [trim]; [trim] ")
            else
                command.append("[0:v] ")

            val width = (cFrame.width * cFrame.scaleX * factor).toInt()
            val height = (cFrame.height * cFrame.scaleY * factor).toInt()
            command.append("scale=$width:$height [scale]; ")

            val isCrop = fCRect.left > fRect.left || fCRect.top > fRect.top || fCRect.right < fRect.right || fCRect.bottom < fRect.bottom
            if(isCrop) {
                val cRect = intersection(fCRect, fRect)
                val cw = if(cRect.width() > width) width
                else cRect.width()
                val ch = if(cRect.height() > height) height
                else cRect.height()

                command.append("[scale] crop=${cw}:${ch}:${cRect.left - fRect.left}:${cRect.top - fRect.top} [crop];")
                command.append("[bg][crop] overlay=${cRect.left}:${cRect.top}:shortest=1 ")
            }
            else {
                command.append("[bg][scale] overlay=${fRect.left}:${fRect.top}:shortest=1 ")
            }
            if(boomerang)
                command.append(", split[out1][out2]; [out2] reverse[r];[out1][r] concat=n=2:v=1 ")

            val f = templateFrame
            if(f != null){
                val themeDuration = if(boomerang) (maxProgress-minProgress) * 2
                else maxProgress-minProgress
                val frameDuration = if(f.type == ThemeFrame.TYPE_GIF) {
                    FFprobe.getMediaInformation(f.getFrame(context).file.absolutePath).duration
                }
                else themeDuration

                val subCommand = StringBuilder()
                if(f.repeatMode == ThemeFrame.REPEAT_MODE_LOOP)
                    subCommand.append(" -ignore_loop 0 ")
                subCommand.append(" -i '${f.getFrame(context).file.absolutePath}' ")

                command.insert(0, subCommand.toString())

                var setpts = ""
                var overlay = "overlay"
                when(f.repeatMode){
                    ThemeFrame.REPEAT_MODE_SYNC->{
                        setpts = if(boomerang) ",setpts=($themeDuration/2/$frameDuration)*PTS "
                        else ",setpts=($themeDuration/$frameDuration)*PTS "
                        overlay = "overlay=shortest=1"
                    }
                    ThemeFrame.REPEAT_MODE_LOOP->{
                        val repeatCount = themeDuration / frameDuration
                        setpts =
                            if(repeatCount == 0L) ",setpts=($themeDuration/$frameDuration)*PTS "
                            else ",setpts=($themeDuration/$repeatCount/$frameDuration)*PTS "
                        overlay = "overlay=shortest=1"
                    }
                    ThemeFrame.REPEAT_MODE_ONE_STOP->{ }
                    ThemeFrame.REPEAT_MODE_ONE_TIME->{
                        overlay = "overlay=eof_action=pass"
                    }
                }

                var crop = ""
                if(f.scaleType == ThemeFrame.SCALE_TYPE_CROP)
                    crop = "crop='if(gt(iw/ih,${size.x}/${size.y}),ih/${size.y}*${size.x},iw)':'if(gt(iw/ih,${size.x}/${size.y}),ih,iw/${size.x}*${size.y})', "
                command.append("[video];[1:v] ${crop}scale=${size.x}x${size.y}$setpts [frame];")
                command.append("[video][frame] $overlay,fps=$frameRate  [result]\" -map [result] ")
            }
            else{
                command.append(",fps=$frameRate [video]\" -map [video] ")
            }

            return command.toString()
        }
    }

    companion object{
        const val MODE_NONE = -1
    }
}