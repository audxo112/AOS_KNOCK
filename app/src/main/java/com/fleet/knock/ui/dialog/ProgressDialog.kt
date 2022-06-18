package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogProgressBinding

class ProgressDialog : CompatDialog(){
    private lateinit var binding:DialogProgressBinding

    init{
        isAutoCancel = false
        isTransparent = false
    }

    val progress = MutableLiveData(0)
    val progressText = Transformations.map(progress) {
        "$it%"
    }

    private var onCancelListener:()->Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.apply{
            attributes.windowAnimations = R.style.BottomUpDialogStyle
            setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<DialogProgressBinding>(inflater, R.layout.dialog_progress, container, false).apply{
            lifecycleOwner = viewLifecycleOwner
            dialog = this@ProgressDialog
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.cancel.setOnClickListener {
            onCancelListener()
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        initDialog()
    }

    private fun initDialog(){
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPixel(160).toInt() + getNavHeight()
        )

        progress.value = 0
        binding.progressText.text = "0%"
    }

    fun setProgress(p: Int) {
        progress.postValue(
            if (p > 100) 100
            else p
        )
    }

    fun setOnCancelListener(listener:()->Unit){
        onCancelListener = listener
    }
}