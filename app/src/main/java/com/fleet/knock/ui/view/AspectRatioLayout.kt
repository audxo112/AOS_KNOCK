package com.fleet.knock.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

class AspectRatioLayout constructor(
    context: Context,
    attrs: AttributeSet? =  /* attrs= */null
) : FrameLayout(context, attrs) {
    var videoAspectRatio = 0f
        set(value){
            if(field != value){
                field = value
                requestLayout()
            }
        }

    var resizeMode = RESIZE_MODE_FIT
        set(value){
            translationX = 0f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
            field = value
            requestLayout()
        }

    val isVertical
        get() = rotation == 0f || rotation == 180f

    private var videoWidth = 0
    private var videoHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (videoAspectRatio <= 0) {
            return
        }

        var width = measuredWidth
        var height = measuredHeight

        val viewAspectRatio = width.toFloat() / height
        val aspectDeformation = videoAspectRatio / viewAspectRatio - 1

        if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            return
        }

        when (resizeMode) {
            RESIZE_MODE_FIXED_WIDTH -> height =
                (width / videoAspectRatio).toInt()
            RESIZE_MODE_FIXED_HEIGHT -> width =
                (height * videoAspectRatio).toInt()
            RESIZE_MODE_ZOOM -> if (aspectDeformation > 0) {
                width = if(isVertical) (height * videoAspectRatio).toInt()
                else (height / videoAspectRatio).toInt()
            } else {
                height = if(isVertical) (width / videoAspectRatio).toInt()
                else (width * videoAspectRatio).toInt()
            }
            RESIZE_MODE_FIT -> if (aspectDeformation > 0) {
                height = if(isVertical) (width / videoAspectRatio).toInt()
                else (width * videoAspectRatio).toInt()
            } else {
                width = if(isVertical) (height * videoAspectRatio).toInt()
                else (height / videoAspectRatio).toInt()
            }
            RESIZE_MODE_FILL -> {
            }
            else -> {
            }
        }

        videoWidth = if(isVertical) width else height
        videoHeight = if(isVertical) height else width

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(videoWidth , MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(videoHeight , MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val l = (width - videoWidth) / 2
        val t = (height - videoHeight) / 2
        val r = l + videoWidth
        val b = t + videoHeight

        super.onLayout(changed, l, t, r, b)
    }

    companion object {
        const val RESIZE_MODE_FIT = 0
        const val RESIZE_MODE_FIXED_WIDTH = 1
        const val RESIZE_MODE_FIXED_HEIGHT = 2
        const val RESIZE_MODE_FILL = 3
        const val RESIZE_MODE_ZOOM = 4

        private const val MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f
    }
}
