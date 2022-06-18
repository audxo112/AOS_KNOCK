package com.fleet.knock.ui.page.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivitySignupCompleteBinding
import com.fleet.knock.ui.page.main.MainActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class SignUpCompleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupCompleteBinding

    private var simplePlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivitySignupCompleteBinding>(this, R.layout.activity_signup_complete).apply {
            lifecycleOwner = this@SignUpCompleteActivity
        }

        binding.end.setOnClickListener {
            gotoMain()
        }

        simplePlayer = SimpleExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }

        binding.player.apply{
            player = simplePlayer
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            useController = false
        }

        simplePlayer?.prepare(RawResourceDataSource(this).let{ dataSource ->
            dataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.mp4_welcome)))
            DataSource.Factory { dataSource }.let{factory ->
                ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
            }
        })
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

    private fun gotoMain(){
        Intent(this, MainActivity::class.java).also{
            startActivity(it)
            finish()
        }

    }
}