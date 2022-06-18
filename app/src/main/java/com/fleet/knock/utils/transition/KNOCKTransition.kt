package com.fleet.knock.utils.transition

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues
import com.fleet.knock.ui.view.CornerView
import com.fleet.knock.ui.view.GradientTextView

class KNOCKTransition : Transition() {
    private val evaluator = ArgbEvaluator()

    override fun getTransitionProperties(): Array<String>? {
        return TRANSITION_PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues){
        if(transitionValues.view is CornerView){
            val view = transitionValues.view
            if(view is CornerView){
                transitionValues.values[PROPNAME_TOP_CORNER] = view.topCornerRadius
                transitionValues.values[PROPNAME_BOTTOM_CORNER] = view.bottomCornerRadius
            }
            else if(view is GradientTextView){
                transitionValues.values[PROPNAME_TOP_CORNER] = view.topCornerRadius
                transitionValues.values[PROPNAME_BOTTOM_CORNER] = view.bottomCornerRadius
                transitionValues.values[PROPNAME_GRADIENT_COLOR] = view.gradientColor
            }
        }
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if(startValues == null || endValues == null)
            return null

        val view = endValues.view

        val animator = ValueAnimator.ofFloat(0f, 1.0f)
        animator.addUpdateListener {
            for(key in TRANSITION_PROPERTIES) {
                when (key) {
                    PROPNAME_TOP_CORNER -> {
                        val value = getFloatValue(key, it.animatedFraction, startValues, endValues)
                        if(view is CornerView)
                            view.topCornerRadius = value
                        else if(view is GradientTextView)
                            view.topCornerRadius = value
                    }
                    PROPNAME_BOTTOM_CORNER ->{
                        val value = getFloatValue(key, it.animatedFraction, startValues, endValues)
                        if(view is CornerView)
                            view.bottomCornerRadius = value
                        else if(view is GradientTextView)
                            view.bottomCornerRadius = value
                    }
                    PROPNAME_GRADIENT_COLOR ->{
                        val value = getColorValue(key, it.animatedFraction, startValues, endValues)
                        if(view is GradientTextView)
                            view.gradientColor = value
                    }
                }
            }
        }

        return animator
    }

    private fun getFloatValue(key:String, fraction:Float, startValues: TransitionValues?, endValues: TransitionValues?) : Float{
        val start = startValues?.values?.get(key) as Float? ?: 0f
        val end = endValues?.values?.get(key) as Float? ?: 0f
        return start + (end - start) * fraction
    }

    private fun getColorValue(key:String, fraction:Float, startValues: TransitionValues?, endValues: TransitionValues?) : Int{
        val start = startValues?.values?.get(key) as Int? ?: 0
        val end = endValues?.values?.get(key) as Int? ?: 0

        return evaluator.evaluate(fraction, start, end) as Int
    }

    companion object{
        const val PROPNAME_TOP_CORNER = "com.fleet.knock:KNOCKTransition:topCornerRadius"
        const val PROPNAME_BOTTOM_CORNER = "com.fleet.knock:KNOCKTransition:bottomCornerRadius"

        const val PROPNAME_GRADIENT_COLOR = "com.fleet.knock:KNOCKTransition:gradientColor"

        val TRANSITION_PROPERTIES = arrayOf(
            PROPNAME_TOP_CORNER,
            PROPNAME_BOTTOM_CORNER,
            PROPNAME_GRADIENT_COLOR
        )
    }
}