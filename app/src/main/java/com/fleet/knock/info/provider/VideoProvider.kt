package com.fleet.knock.info.provider

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource

class VideoProvider(context: Context, vararg paths:String) : FileProvider(context, *paths){
    val videoDataSpec
        get() = if(isExistFile) DataSpec(Uri.fromFile(file)) else null

    val videoSource
        get() = if(isExistFile) {
            FileDataSource().let { dataSource ->
                dataSource.open(videoDataSpec)
                DataSource.Factory { dataSource }.let { factory ->
                    ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
                }
            }
        }
        else{
            null
        }
}