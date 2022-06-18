package com.fleet.knock.ui.page.main

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityMainBinding
import com.fleet.knock.ui.fragment.CompatFragment
import com.fleet.knock.ui.page.setting.SettingActivity
import com.fleet.knock.utils.*
import com.fleet.knock.utils.tutorial.TutorialNaverCafe
import com.fleet.knock.utils.tutorial.TutorialUtil

class MainActivity : AppCompatActivity(){
    private lateinit var binding : ActivityMainBinding
    private val viewModel:MainViewModel by viewModels()

    private val homeFragment by lazy{
        MainHomeFragment().also{
            bindFragment(R.id.home_fragment_container, it)
        }
    }

    private val promotionFragment by lazy{
        MainPromotionFragment().also{
            bindFragment(R.id.promotion_fragment_container, it)
        }
    }

    private fun bindFragment(containerId:Int, fragment:CompatFragment){
        supportFragmentManager.beginTransaction().apply{
            replace(containerId, fragment)
            addToBackStack(null)
            hide(fragment)
        }.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CURRENT_TIME", "time : ${System.currentTimeMillis()}")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply{
            lifecycleOwner = this@MainActivity
            vm = viewModel

            setting.setOnClickListener {
                gotoSetting()
            }

            naverCafe.setOnClickListener {
                gotoNaverCafe()
            }

            naverCafeSpeech.setOnClickListener {
                viewModel.hideRecommendNaverCafe()
            }

            pageHome.setOnClickListener {
                gotoPageHome()
            }

            pagePromotion.setOnClickListener {
                gotoPagePromotion()
            }
        }

        requestReview()

        changeFragment(promotionFragment)
    }

    override fun onBackPressed() {
        BackPressHandler.get().onBackPressed(this)
    }

    override fun onResume() {
        super.onResume()

        requestNaverCafe()
    }

    private fun changeFragment(fragment:CompatFragment){
        supportFragmentManager.beginTransaction().apply{
            if(fragment != homeFragment) hide(homeFragment)
            if(fragment != promotionFragment) hide(promotionFragment)
            show(fragment)
        }.commit()
    }

    private fun transition(target:Int){
        val current = viewModel.currentPageId(target)

        binding.pageNavigation.run{
            setTransition(current, target)
            setTransitionDuration(300)
            transitionToEnd()
        }
    }

    private fun gotoPageHome(){
        if(viewModel.isSamePage(MainViewModel.PAGE_HOME))
            return

        changeFragment(homeFragment)
        transition(R.id.open_home)

        viewModel.applyPage(MainViewModel.PAGE_HOME)
    }

    private fun gotoPagePromotion(){
        if(viewModel.isSamePage(MainViewModel.PAGE_PROMOTION))
            return

        changeFragment(promotionFragment)
        transition(R.id.open_promotion)

        viewModel.applyPage(MainViewModel.PAGE_PROMOTION)
    }

    private fun requestReview(){
        if(!isFinishing && viewModel.isReview){
            AlertDialog.Builder(this).apply{
                setTitle(R.string.dialog_review_title)
                setMessage(R.string.dialog_review_message)
                setPositiveButton(R.string.dialog_review_yes){_,_->
                    gotoReview()
                }
                setNegativeButton(R.string.dialog_review_no, null)
                show()
            }
        }
    }

    private fun requestNaverCafe(){
        if(TutorialUtil.get().request(application, TutorialNaverCafe.NAME)){
            viewModel.showRecommendNaverCafe()
        }
    }

    private fun gotoReview(){
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun gotoSetting(){
        startActivity(Intent(this, SettingActivity::class.java))
    }

    private fun gotoNaverCafe(){
        viewModel.hideRecommendNaverCafe()
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("navercafe://cafe?cafeUrl=livewallpaper&appId=${packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }catch (e:ActivityNotFoundException){
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://cafe.naver.com/livewallpaper")
            ).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}