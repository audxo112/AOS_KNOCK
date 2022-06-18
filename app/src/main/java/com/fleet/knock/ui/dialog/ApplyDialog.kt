package com.fleet.knock.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogApplyBinding
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable

class ApplyDialog : CompatDialog(){
    private lateinit var binding:DialogApplyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_apply, container, false)
        binding.apply{
            lifecycleOwner = viewLifecycleOwner
        }

        val gif = binding.applyGif.drawable as GifDrawable?
        gif?.addAnimationListener(object:AnimationListener{
            override fun onAnimationCompleted(loopNumber: Int) {
                gif.removeAnimationListener(this)
                try{
                    dismiss()
                }
                catch (e:IllegalStateException){
                    e.printStackTrace()
                }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(dpToPixel(200).toInt(), dpToPixel(200).toInt())
    }
}