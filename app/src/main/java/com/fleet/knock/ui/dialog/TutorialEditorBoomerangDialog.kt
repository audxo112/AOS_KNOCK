package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogTutorialEditorBoomerangBinding

class TutorialEditorBoomerangDialog : CompatDialog() {

    private lateinit var binding: DialogTutorialEditorBoomerangBinding

    private var onApplyCallback = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.apply {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_tutorial_editor_boomerang, container, false)
        binding.apply{
            dialog = this@TutorialEditorBoomerangDialog
            lifecycleOwner = viewLifecycleOwner

            close.setOnClickListener {
                dismiss()
            }

            apply.setOnClickListener {
                onApplyCallback()
                dismiss()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        initDialog()
    }

    private fun initDialog(){
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPixel(324).toInt() + getNavHeight()
        )
    }

    fun setOnApplyCallback(callback:()->Unit){
        onApplyCallback = callback
    }
}