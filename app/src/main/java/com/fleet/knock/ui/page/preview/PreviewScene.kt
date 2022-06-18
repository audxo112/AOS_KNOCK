package com.fleet.knock.ui.page.preview

import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.transition.*
import com.fleet.knock.R
import com.fleet.knock.utils.transition.KNOCKTransition

class PreviewScene{
    private fun getPreviewHomeScene(container:ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        setVisibility(R.id.home_screen, ConstraintSet.VISIBLE)
        setIntValue(R.id.home_screen, "GradientColor", getColor(container, R.color.colorBackgroundWhite))
        setColorValue(R.id.home_screen, "TextColor", getColor(container, R.color.colorTextBlack))

        setVisibility(R.id.lock_screen, ConstraintSet.VISIBLE)
        setIntValue(R.id.lock_screen, "GradientColor", getColor(container, R.color.colorBackgroundBlackA10))
        setColorValue(R.id.lock_screen, "TextColor", getColor(container, R.color.colorTextWhite))

        setVisibility(R.id.preview_home, ConstraintSet.VISIBLE)
        setVisibility(R.id.preview_lock, ConstraintSet.INVISIBLE)
    }

    private fun getPreviewLockScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        setVisibility(R.id.home_screen, ConstraintSet.VISIBLE)
        setIntValue(R.id.home_screen, "GradientColor", getColor(container, R.color.colorBackgroundBlackA10))
        setColorValue(R.id.home_screen, "TextColor", getColor(container, R.color.colorTextWhite))

        setVisibility(R.id.lock_screen, ConstraintSet.VISIBLE)
        setIntValue(R.id.lock_screen, "GradientColor", getColor(container, R.color.colorBackgroundWhite))
        setColorValue(R.id.lock_screen, "TextColor", getColor(container, R.color.colorTextBlack))

        setVisibility(R.id.preview_home, ConstraintSet.INVISIBLE)
        setVisibility(R.id.preview_lock, ConstraintSet.VISIBLE)
    }

    private fun getPreviewScene(container: ConstraintLayout) = ConstraintSet().apply{
        clone(container)

        setVisibility(R.id.home_screen, ConstraintSet.INVISIBLE)
        setIntValue(R.id.home_screen, "GradientColor", getColor(container, R.color.colorBackgroundBlackA10))
        setColorValue(R.id.home_screen, "TextColor", getColor(container, R.color.colorTextWhite))

        setVisibility(R.id.lock_screen, ConstraintSet.INVISIBLE)
        setIntValue(R.id.lock_screen, "GradientColor", getColor(container, R.color.colorBackgroundBlackA10))
        setColorValue(R.id.lock_screen, "TextColor", getColor(container, R.color.colorTextWhite))

        setVisibility(R.id.preview_home, ConstraintSet.INVISIBLE)
        setVisibility(R.id.preview_lock, ConstraintSet.INVISIBLE)
    }

    fun startTransition(container:ConstraintLayout,
                        fade:Boolean = false,
                        onStart:()->Unit = {},
                        onEnd:()->Unit = {}){

        val transition = TransitionSet().apply{
            if(fade) addTransition(Fade(Fade.OUT))
            if(fade) addTransition(Fade(Fade.IN))
            addTransition(ChangeBounds())
            addTransition(KNOCKTransition())
            duration = 300L
            interpolator = DecelerateInterpolator()

            addListener(object: Transition.TransitionListener{
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {
                    onStart()
                }
                override fun onTransitionEnd(transition: Transition) {
                    onEnd()
                }
            })
        }
        TransitionManager.beginDelayedTransition(container, transition)
    }

    fun applyToPreviewLock(container: ConstraintLayout?){
        container?:return
        getPreviewLockScene(container).applyTo(container)
    }

    fun applyToPreviewHome(container:ConstraintLayout?){
        container?:return
        getPreviewHomeScene(container).applyTo(container)
    }

    fun applyToPreview(container: ConstraintLayout?){
        container?:return
        getPreviewScene(container).applyTo(container)
    }

    private fun getColor(view: View, colorResId:Int) = ContextCompat.getColor(view.context, colorResId)
}