package com.fleet.knock.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogTutorialPreviewSwipeBinding
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class TutorialPreviewSwipeDialog : CompatDialog(){

    private lateinit var binding: DialogTutorialPreviewSwipeBinding

    private var onCancelCallback = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_tutorial_preview_swipe, container, false)
        binding.apply{
            dialog = this@TutorialPreviewSwipeDialog
            lifecycleOwner = viewLifecycleOwner

            close.setOnClickListener {
                onCancelCallback()
                dismiss()
            }

            loadGif(binding.explainMotion, R.drawable.gif_swipe_motion)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        initDialog()
    }

    private fun initDialog(){
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun loadGif(view: GifImageView, resId:Int){
        val drawable = GifDrawable.createFromResource(resources, resId) ?: return

        view.setImageDrawable(drawable)
    }

    fun setOnCancelCallback(callback:()->Unit){
        onCancelCallback = callback
    }
}