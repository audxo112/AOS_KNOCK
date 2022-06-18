package com.fleet.knock.utils.recycler

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.fleet.knock.ui.view.AspectRatioLayout
import com.fleet.knock.ui.view.FPlayerView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.video.VideoListener

class ExoPlayerPagerHelper(context: Context){
    private val onPageChangeCallback = object:ViewPager2.OnPageChangeCallback(){
        override fun onPageScrollStateChanged(state: Int) {
            val ip = state == ViewPager2.SCROLL_STATE_IDLE
            if(ip)
                currentPos.value = pager?.currentItem

            if(idlePager.value != ip)
                idlePager.value = ip
        }
    }

    var pager : ViewPager2? = null
    set(value){
        value?.offscreenPageLimit = 3
        value?.registerOnPageChangeCallback(onPageChangeCallback)
        field = value
    }

    private val simplePlayer = SimpleExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        volume = 0.0f
    }

    private val exoPlayerView = FPlayerView(context).apply{
        resizeMode = AspectRatioLayout.RESIZE_MODE_ZOOM
        player = simplePlayer
        preventEvent = true

        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    val currentPos = MutableLiveData<Int>()
    val idlePager = MutableLiveData<Boolean>()

    fun bind(container:ViewGroup?, source: ProgressiveMediaSource?, onReadyCallback:()->Unit = {}){
        container?: return
        source?: return

        loadVideo(source)

        (exoPlayerView.parent as ViewGroup?)?.removeView(exoPlayerView)
        container.addView(exoPlayerView)

        simplePlayer.addVideoListener(object :VideoListener{
            override fun onRenderedFirstFrame() {
                simplePlayer.removeVideoListener(this)
                onReadyCallback()
            }
        })
    }

    private fun loadVideo(source: ProgressiveMediaSource?){
        source ?: return
        try {
            val beforePlayWhenReady = simplePlayer.playWhenReady
            simplePlayer.playWhenReady = false
            simplePlayer.stop()
            simplePlayer.prepare(source)
            simplePlayer.playWhenReady = beforePlayWhenReady
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun setAdapter(baseAdapter: BaseAdapter){
        pager?.adapter = baseAdapter
    }

    fun add(list:List<Any?>, selectedPos:Int? = null){
        val adapter = pager?.adapter as BaseAdapter? ?: return

        adapter.addAll(list)

        selectedPos ?: return

        currentPos.postValue(selectedPos)

        pager?.setCurrentItem(selectedPos, false)
    }

    fun delete(pos:Int){
        val adapter = pager?.adapter as BaseAdapter? ?:return

        val lastPos = pager?.adapter?.itemCount ?: 0

        currentPos.postValue(
            if(pos >= lastPos - 1 ) pos -1
            else pos
        )

        adapter.delete(pos)
    }

    fun isEmpty() : Boolean{
        val adapter = pager?.adapter as BaseAdapter? ?: return true

        return adapter.isEmpty()
    }

    fun resume(){
        simplePlayer.playWhenReady = true
    }

    fun pause(){
        simplePlayer.playWhenReady = false
    }

    fun destroy(){
        pager?.unregisterOnPageChangeCallback(onPageChangeCallback)

        simplePlayer.stop()
        simplePlayer.release()
    }
}