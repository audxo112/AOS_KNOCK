package com.fleet.knock.info.provider

import android.content.Context
import android.graphics.drawable.Drawable
import pl.droidsonroids.gif.GifDrawable

class ImageProvider(context: Context, vararg paths:String) : FileProvider(context, *paths){
    val image
        get() = if(isExistFile) Drawable.createFromPath(file.absolutePath) else null

    val gif
        get() = if(isExistFile) GifDrawable(file.absolutePath) else null
}