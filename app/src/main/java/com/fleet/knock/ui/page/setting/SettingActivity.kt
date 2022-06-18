package com.fleet.knock.ui.page.setting

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivitySettingBinding
import com.fleet.knock.utils.WallpaperUtil

class SettingActivity : AppCompatActivity(){
    private lateinit var binding:ActivitySettingBinding
    private val viewModel :SettingViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        binding.apply{
            lifecycleOwner = this@SettingActivity
            vm = viewModel

            binding.back.setOnClickListener {
                onBackPressed()
            }

            binding.enableTheme.setOnClickListener{
                toggleSetWallpaper()
            }

            binding.share.setOnClickListener{
                gotoShare()
            }

            binding.review.setOnClickListener {
                gotoReview()
            }

            binding.alwaysQuestion.setOnClickListener {
                gotoAlwaysQuestion()
            }

            binding.developTool.setOnClickListener {
                gotoDevelopTool()
            }
        }

        viewModel.enableTheme.observe(this, Observer {
            showSettingTheme(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == WallpaperUtil.REQUEST_SET_WALLPAPER){
            val isSetWallpaper = WallpaperUtil.isSetWallpaper(applicationContext)
            viewModel.isSetWallpaper.value = isSetWallpaper

            if(isSetWallpaper){
                viewModel.logSetWallpaper()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun toggleSetWallpaper(){
        val isSetWallpaper = viewModel.isSetWallpaper.value == true

        if(isSetWallpaper){
            viewModel.logClearWallpaper()
            WallpaperUtil.removeWallpaper(this@SettingActivity,
                onProgress = {
                    viewModel.isLoading.postValue(it)
                },
                onComplete = {
                    viewModel.isSetWallpaper.value = false
                })
        }
        else{
            WallpaperUtil.setWallpaper(this@SettingActivity, WallpaperUtil.REQUEST_SET_WALLPAPER)
        }
    }

    private fun showSettingTheme(show:Boolean){
        ChangeBounds().apply{
            interpolator = AccelerateInterpolator()
            if(viewModel.isCreated)
                duration = 0
            TransitionManager.beginDelayedTransition(binding.root, this)
        }

        viewModel.isCreated = false
    }

    private fun gotoShare(){
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.activity_setting_share_sentence))
            startActivity(Intent.createChooser(this, getString(R.string.activity_setting_share_title)))
        }
    }

    private fun gotoReview(){
        try {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        } catch (e: ActivityNotFoundException) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }

    private fun gotoAlwaysQuestion(){
        Intent(Intent.ACTION_VIEW, Uri.parse("https://knock-lab.tistory.com/2")).apply{
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    private fun gotoDevelopTool(){
        val intent = Intent(this, SettingDevelopActivity::class.java)
        startActivity(intent)
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}