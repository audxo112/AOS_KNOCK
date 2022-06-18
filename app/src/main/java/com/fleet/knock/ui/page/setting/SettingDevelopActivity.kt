package com.fleet.knock.ui.page.setting

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivitySettingDevelopBinding

class SettingDevelopActivity : AppCompatActivity(){
    private lateinit var binding:ActivitySettingDevelopBinding
    private val viewModel:SettingDevelopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_develop)
        binding.apply{
            lifecycleOwner = this@SettingDevelopActivity
            vm = viewModel

            back.setOnClickListener {
                onBackPressed()
            }

            encodingQualityOptimization.setOnClickListener {
                viewModel.toggleEncodingQualityOptimization()
            }

            encodingApplyAdBlock.setOnClickListener {
                viewModel.toggleEncodingApplyAdBlock()
            }

            localApplyAdBlock.setOnClickListener {
                viewModel.toggleLocalApplyAdBlock()
            }

            promotionApplyAdBlock.setOnClickListener {
                viewModel.togglePromotionApplyAdBlock()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}