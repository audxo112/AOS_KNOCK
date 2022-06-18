package com.fleet.knock.utils.encoding

import android.content.Context
import android.net.Uri
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class GifEncodingJob : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancel(){
        FFmpeg.cancel()
        job.cancel()
    }

    suspend fun execute(context: Context, uri:Uri) : Uri? = withContext(coroutineContext){
        try {
            val dir = context.getDir("editor", Context.MODE_PRIVATE).apply {
                if (!exists()) mkdir()
            }
            val tmp = File(dir, "gif_tmp.mp4")
            val command = StringBuilder().apply {
                append("-i '${uri.path}' ")
                append("-movflags faststart ")
                append("-pix_fmt yuv420p ")
                append("-vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' ")
                append("-vcodec libx264 ")
                append("-y ")
                append("-an ")
                append("'${tmp.absolutePath}'")
            }

            val result = FFmpeg.execute(command.toString())
            if (result == 0)
                Uri.fromFile(tmp)
            else
                null
        }
        catch (e:Exception){
            FFmpeg.cancel()
            null
        }
    }
}