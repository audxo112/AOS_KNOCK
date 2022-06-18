package com.fleet.knock.ui.page.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivitySplashBinding
import com.fleet.knock.ui.page.main.MainActivity
import com.fleet.knock.utils.admob.BaseADUtil
import com.fleet.knock.utils.admob.PromotionADUtil
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener

class SplashActivity : AppCompatActivity(){
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    private var simplePlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BaseADUtil.get(application)
        PromotionADUtil.get(application)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.apply{
            lifecycleOwner = this@SplashActivity
            vm = viewModel
        }

        simplePlayer = SimpleExoPlayer.Builder(application).build().apply{
            repeatMode = Player.REPEAT_MODE_ONE
            addVideoListener(object: VideoListener {
                override fun onRenderedFirstFrame() {
                    removeVideoListener(this)
                    binding.preload.visibility = View.INVISIBLE
                }
            })
        }

        binding.player.apply{
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player = simplePlayer
            preventEvent = true
        }

        viewModel.isComplete.observe(this, Observer {complete ->
            if(complete) startMainActivity()
        })

        openScreen()
    }

    override fun onDestroy() {
        simplePlayer?.stop()
        simplePlayer?.release()
        simplePlayer = null

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        simplePlayer?.playWhenReady = true
    }

    override fun onPause() {
        simplePlayer?.playWhenReady = false

        super.onPause()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun openScreen(){
        binding.shadow.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.anim_fade_in)
        )
    }

    private fun startMainActivity(){
        if(!isFinishing)
            Intent(this, MainActivity::class.java).also{
                startActivity(it)
                finish()
            }
    }
}