package com.fleet.knock.ui.page.preview

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.*
import com.fleet.knock.R
import com.fleet.knock.utils.transition.KNOCKTransition

class PreviewPublicHolderScene{
    private fun getContentScene(container:ConstraintLayout) = ConstraintSet().apply{
        load(container.context, R.xml.scene_public_preview_close_detail)

        setVisibilityMode(R.id.badge, ConstraintSet.VISIBILITY_MODE_IGNORE)

        setFloatValue(R.id.detail_container, "BottomCornerRadius", dpToFloat(container.context, 12))
    }

    private fun getContentDetailScene(container:ConstraintLayout) = ConstraintSet().apply{
        load(container.context, R.xml.scene_public_preview_open_detail)

        setVisibilityMode(R.id.badge, ConstraintSet.VISIBILITY_MODE_IGNORE)

        setFloatValue(R.id.detail_container, "BottomCornerRadius", 0f)
    }

    private fun getHideContent(container:ConstraintLayout) = ConstraintSet().apply{
        load(container.context, R.xml.scene_public_preview_hide_detail)

        setVisibilityMode(R.id.badge, ConstraintSet.VISIBILITY_MODE_IGNORE)

        setFloatValue(R.id.detail_container, "BottomCornerRadius", dpToFloat(container.context, 12))
    }

    private fun getCloseDetail(container: ConstraintLayout) = ConstraintSet().apply{
        load(container.context, R.xml.scene_public_preview_close_detail)

        setVisibilityMode(R.id.badge, ConstraintSet.VISIBILITY_MODE_IGNORE)
    }

    private fun getOpenDetail(container: ConstraintLayout) = ConstraintSet().apply{
        load(container.context, R.xml.scene_public_preview_open_detail)

        setVisibilityMode(R.id.badge, ConstraintSet.VISIBILITY_MODE_IGNORE)
    }

    fun applyToContent(container: ConstraintLayout?, detail:ConstraintLayout?){
        container?:return
        detail?:return
        getContentScene(container).applyTo(container)
        getCloseDetail(detail).applyTo(detail)
    }

    fun applyToContentDetail(container: ConstraintLayout?, detail:ConstraintLayout?){
        container?:return
        detail?:return
        getContentDetailScene(container).applyTo(container)
        getOpenDetail(detail).applyTo(detail)
    }

    fun applyToHideContent(container: ConstraintLayout?, detail:ConstraintLayout?){
        container?:return
        detail?:return
        getHideContent(container).applyTo(container)
        getCloseDetail(detail).applyTo(detail)
    }

    private fun dpToPixel(view: View, dp:Int) = dpToFloat(view.context, dp).toInt()

    private fun dpToFloat(view:View, dp:Int) = dpToFloat(view.context, dp)

    private fun dpToFloat(context: Context, dp:Int) = context.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }
}