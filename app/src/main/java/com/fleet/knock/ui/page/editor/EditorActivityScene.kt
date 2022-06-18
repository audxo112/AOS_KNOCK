package com.fleet.knock.ui.page.editor

import android.app.Application
import android.util.DisplayMetrics
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.transition.addListener
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.transition.*
import com.fleet.knock.R

class EditorActivityScene(private val application: Application) {
    private fun getEditorScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        connect(R.id.player, ConstraintSet.TOP, R.id.save, ConstraintSet.BOTTOM)
        connect(R.id.player, ConstraintSet.BOTTOM, R.id.controller_pos, ConstraintSet.TOP)
        setMargin(R.id.player, ConstraintSet.BOTTOM, dpToPixel(22))

        clear(R.id.save, ConstraintSet.START)
        setMargin(R.id.save, ConstraintSet.END, dpToPixel(9))
        connect(R.id.save, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        setVisibilityMode(R.id.trim_help, ConstraintSet.VISIBILITY_MODE_IGNORE)

        connect(R.id.tool_frame_container, ConstraintSet.TOP, R.id.controller_pos, ConstraintSet.TOP)
        connect(R.id.tool_frame_container, ConstraintSet.BOTTOM, R.id.inset, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_frame_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        connect(R.id.tool_trim_container, ConstraintSet.TOP, R.id.controller_pos, ConstraintSet.TOP)
        connect(R.id.tool_trim_container, ConstraintSet.BOTTOM, R.id.inset, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_trim_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        connect(R.id.tool_bg_color_container, ConstraintSet.TOP, R.id.controller_pos, ConstraintSet.TOP)
        connect(R.id.tool_bg_color_container, ConstraintSet.BOTTOM, R.id.inset, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_bg_color_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_nav_container, ConstraintSet.TOP)
        connect(R.id.tool_nav_container, ConstraintSet.BOTTOM, R.id.inset, ConstraintSet.BOTTOM)

        setVisibility(R.id.warning, ConstraintSet.INVISIBLE)
        setVisibility(R.id.warning_confirm, ConstraintSet.INVISIBLE)

        setVisibility(R.id.preview, ConstraintSet.INVISIBLE)

        setVisibility(R.id.home_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.home_preview, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_preview, ConstraintSet.INVISIBLE)

        setVisibility(R.id.apply, ConstraintSet.INVISIBLE)
        setVisibility(R.id.save_on_device, ConstraintSet.INVISIBLE)
    }

    private fun getWarningTooLongScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        connect(R.id.player, ConstraintSet.TOP, R.id.save, ConstraintSet.BOTTOM)
        connect(R.id.player, ConstraintSet.BOTTOM, R.id.controller_pos, ConstraintSet.TOP)
        setMargin(R.id.player, ConstraintSet.BOTTOM, dpToPixel(22))

        clear(R.id.save, ConstraintSet.END)
        setMargin(R.id.save, ConstraintSet.START, dpToPixel(9))
        connect(R.id.save, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)

        setVisibilityMode(R.id.trim_help, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_frame_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_frame_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_frame_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_trim_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_trim_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_trim_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_bg_color_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_bg_color_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_bg_color_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_nav_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_nav_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        setVisibility(R.id.warning, ConstraintSet.VISIBLE)
        setVisibility(R.id.warning_confirm, ConstraintSet.VISIBLE)

        setVisibility(R.id.preview, ConstraintSet.INVISIBLE)

        setVisibility(R.id.home_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.home_preview, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_preview, ConstraintSet.INVISIBLE)

        setVisibility(R.id.apply, ConstraintSet.INVISIBLE)
        setVisibility(R.id.save_on_device, ConstraintSet.INVISIBLE)
    }

    private fun getCompleteBaseScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        connect(R.id.player, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(R.id.player, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setMargin(R.id.player, ConstraintSet.BOTTOM, dpToPixel(0))

        clear(R.id.save, ConstraintSet.END)
        setMargin(R.id.save, ConstraintSet.START, dpToPixel(9))
        connect(R.id.save, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)

        setVisibilityMode(R.id.trim_help, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_frame_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_frame_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_frame_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_trim_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_trim_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_trim_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_bg_color_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_bg_color_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        setVisibilityMode(R.id.tool_bg_color_container, ConstraintSet.VISIBILITY_MODE_IGNORE)

        clear(R.id.tool_nav_container, ConstraintSet.BOTTOM)
        connect(R.id.tool_nav_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        setVisibility(R.id.warning, ConstraintSet.INVISIBLE)
        setVisibility(R.id.warning_confirm, ConstraintSet.INVISIBLE)

        setVisibility(R.id.preview, ConstraintSet.VISIBLE)

        setVisibility(R.id.home_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_screen, ConstraintSet.INVISIBLE)
        setVisibility(R.id.home_preview, ConstraintSet.INVISIBLE)
        setVisibility(R.id.lock_preview, ConstraintSet.INVISIBLE)

        setVisibility(R.id.apply, ConstraintSet.INVISIBLE)
        setVisibility(R.id.save_on_device, ConstraintSet.INVISIBLE)

        setAlpha(R.id.lock_screen, 0f)
        setAlpha(R.id.home_screen, 0f)
        setAlpha(R.id.lock_preview, 0f)
        setAlpha(R.id.home_preview, 0f)
    }

    private fun getCompleteLockPreviewScene(container: ConstraintLayout) = getCompleteBaseScene(container).apply{
        setVisibility(R.id.home_screen, ConstraintSet.VISIBLE)
        setVisibility(R.id.lock_screen, ConstraintSet.VISIBLE)
        setVisibility(R.id.lock_preview, ConstraintSet.VISIBLE)

        setAlpha(R.id.lock_screen, 1f)
        setAlpha(R.id.home_screen, 1f)
        setAlpha(R.id.lock_preview, 1f)
    }

    private fun getCompleteHomePreviewScene(container: ConstraintLayout) = getCompleteBaseScene(container).apply{
        setVisibility(R.id.home_screen, ConstraintSet.VISIBLE)
        setVisibility(R.id.lock_screen, ConstraintSet.VISIBLE)
        setVisibility(R.id.home_preview, ConstraintSet.VISIBLE)

        setAlpha(R.id.lock_screen, 1f)
        setAlpha(R.id.home_screen, 1f)
        setAlpha(R.id.home_preview, 1f)
    }

    private fun getCompleteScene(container: ConstraintLayout) = getCompleteBaseScene(container).apply{
        setVisibility(R.id.apply, ConstraintSet.VISIBLE)
        setVisibility(R.id.save_on_device, ConstraintSet.VISIBLE)
    }

    private fun gotoPage(container:ConstraintLayout,
                         set:ConstraintSet,
                         anim:Boolean = true,
                         onStart:()->Unit = {},
                         onEnd:()->Unit = {}){
        set.applyTo(container)

        if(anim){
            val transition = TransitionSet().apply{
                addTransition(Fade(Fade.IN))
                addTransition(Fade(Fade.OUT))
                addTransition(ChangeBounds())
                duration = 300L
                interpolator = DecelerateInterpolator()

                addListener(object:Transition.TransitionListener{
                    override fun onTransitionEnd(transition: Transition) {
                        onEnd()
                    }
                    override fun onTransitionResume(transition: Transition) {}
                    override fun onTransitionPause(transition: Transition) {}
                    override fun onTransitionCancel(transition: Transition) {}
                    override fun onTransitionStart(transition: Transition) {
                        onStart()
                    }
                })
            }
            TransitionManager.beginDelayedTransition(container, transition)
        }
        else{
            onStart()
            onEnd()
        }
    }

    fun gotoEditor(container:ConstraintLayout, anim:Boolean = true, onStart:()->Unit = {}, onEnd:()->Unit = {}){
        gotoPage(container, getEditorScene(container), anim, onStart, onEnd)
    }

    fun gotoWarningTooLong(container:ConstraintLayout, anim:Boolean = true, onStart:()->Unit = {}, onEnd:()->Unit = {}){
        gotoPage(container, getWarningTooLongScene(container), anim, onStart, onEnd)
    }

    fun gotoCompleteLockPreview(container:ConstraintLayout){
        gotoPage(container, getCompleteLockPreviewScene(container))
    }

    fun gotoCompleteHomePreview(container:ConstraintLayout){
        gotoPage(container, getCompleteHomePreviewScene(container))
    }

    fun gotoComplete(container: ConstraintLayout, anim:Boolean = true, onStart:()->Unit = {}, onEnd:()->Unit = {}){
        gotoPage(container, getCompleteScene(container), anim, onStart, onEnd)
    }

    private fun getToolIndicatorScene(container:ConstraintLayout, targetId:Int) = ConstraintSet().apply{
        clone(container)
        connect(R.id.tool_indicator, ConstraintSet.START, targetId, ConstraintSet.START)
        connect(R.id.tool_indicator, ConstraintSet.END, targetId, ConstraintSet.END)
    }

    fun toolIndicator(container: ConstraintLayout, targetId:Int, anim:Boolean = true){
        getToolIndicatorScene(container, targetId).applyTo(container)

        if(anim){
            TransitionManager.beginDelayedTransition(container, ChangeBounds().apply{
                duration = 100L
                interpolator = LinearInterpolator()
            })
        }
    }

    fun dpToPixel(dp:Int) = application.resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}