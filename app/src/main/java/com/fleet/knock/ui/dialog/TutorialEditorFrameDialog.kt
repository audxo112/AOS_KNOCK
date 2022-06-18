package com.fleet.knock.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogTutorialEditorFrameBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class
TutorialEditorFrameDialog : CompatDialog(){
    private lateinit var binding:DialogTutorialEditorFrameBinding

    private var autoCancelJog: Job? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply{
            window?.attributes = window?.attributes?.apply{
                y = getStaHeight(context) + dpToPixel(180).toInt()
            }
            window?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_tutorial_editor_frame, container, false)
        binding.apply{
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(dpToPixel(160).toInt(), dpToPixel(160).toInt())

        autoCancel()
    }

    override fun onPause() {
        super.onPause()

        autoCancelJog?.cancel()
    }

    private fun autoCancel(){
        autoCancelJog?.cancel()
        autoCancelJog = lifecycleScope.launch {
            delay(3000)
            try {
                if(isActive)
                    dismiss()
            }
            catch(e:IllegalAccessException){
                e.printStackTrace()
            }
        }
    }
}