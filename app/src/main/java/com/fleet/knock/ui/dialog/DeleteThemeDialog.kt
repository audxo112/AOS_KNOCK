package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogDeleteThemeBinding

class DeleteThemeDialog : CompatDialog(){
    private lateinit var binding:DialogDeleteThemeBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply{
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_delete_theme, container, false)
        binding.apply{
            yes.setOnClickListener{
                onThemeDelete()
                dismiss()
            }
            no.setOnClickListener {
                dismiss()
            }
        }

        return binding.root
    }

    private var onThemeDelete :() ->Unit = {}

    fun setOnThemeDeleteListener(listener:()->Unit){
        onThemeDelete = listener
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(dpToPixel(210).toInt(), dpToPixel(210).toInt())
    }
}