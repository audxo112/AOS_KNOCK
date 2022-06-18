package com.fleet.knock.ui.page.preview

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.fleet.knock.R

class PreviewLocalHolderScene {
    private fun getContentScene(container:ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        clear(R.id.more, ConstraintSet.START)
        connect(R.id.more, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPixel(container, 9))

        clear(R.id.close_preview, ConstraintSet.END)
        connect(R.id.close_preview, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPixel(container, 9))

        clear(R.id.apply, ConstraintSet.TOP)
        connect(R.id.apply, ConstraintSet.BOTTOM, R.id.inset, ConstraintSet.BOTTOM, dpToPixel(container, 40))
    }

    private fun getHideContentScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        clear(R.id.more, ConstraintSet.END)
        connect(R.id.more, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPixel(container, 9))

        clear(R.id.close_preview, ConstraintSet.START)
        connect(R.id.close_preview, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPixel(container, 9))

        clear(R.id.apply, ConstraintSet.BOTTOM)
        connect(R.id.apply, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPixel(container, 20))
    }

    fun applyToContent(container:ConstraintLayout?){
        container ?: return

        getContentScene(container).applyTo(container)
    }

    fun applyToHideContent(container: ConstraintLayout?){
        container ?: return

        getHideContentScene(container).applyTo(container)
    }

    private fun dpToPixel(view: View, dp:Int) = dpToFloat(view.context, dp).toInt()

    private fun dpToFloat(view: View, dp:Int) = dpToFloat(view.context, dp)

    private fun dpToFloat(context: Context, dp:Int) = context.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }
}