package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogRevertEditorBinding

class RevertEditorDialog : CompatDialog(){
    private lateinit var binding:DialogRevertEditorBinding

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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_revert_editor, container, false)
        binding.apply{
            gotoMain.setOnClickListener {
                gotoMain()
                dismiss()
            }

            gotoEdit.setOnClickListener {
                gotoEdit()
                dismiss()
            }
        }

        return binding.root
    }

    private var gotoMain : () -> Unit = {}

    private var gotoEdit : () -> Unit = {}

    fun setGotoMain(listener:()->Unit){
        gotoMain = listener
    }

    fun setGotoEdit(listener:()->Unit){
        gotoEdit = listener
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(dpToPixel(210).toInt(), dpToPixel(210).toInt())
    }
}