package com.fleet.knock.utils.encoding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.UriUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.NumberFormatException
import kotlin.coroutines.CoroutineContext

class LocalThemeEncodingJob : CoroutineScope{
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private var progressCallback : (frameNumber:Int) -> Unit = {}

    fun cancel(){
        crashlytics.log("request local theme encoding cancel")
        FFmpeg.cancel()
        job.cancel()
    }

    fun setProgressCallback(callback:(progress:Int)->Unit){
        progressCallback = callback
    }

    suspend fun execute(context: Context,
                        uri: Uri,
                        local:FThemeLocal,
                        filterComplex:String) : Int = withContext(coroutineContext){

        try {
            Config.resetStatistics()
            Config.enableStatisticsCallback { s ->
                progressCallback(s.videoFrameNumber)
            }

            val result = encoding(context, uri, local, filterComplex)
            if(result != RESULT_OK){
                return@withContext result
            }
            return@withContext makeThemeImage(context, local)
        }
        catch (e:CancellationException){
            FFmpeg.cancel()
            return@withContext RESULT_CANCEL
        }
    }

    private fun encoding(context: Context,
                         uri:Uri,
                         local:FThemeLocal,
                         filterComplex:String) : Int{
        val src = UriUtil.getPath(context, uri)
        if(src == "")
            return RESULT_FAILED

        val dst = local.getThemeProvider(context).file
        val command = StringBuilder().apply{
            append("-i \"$src\" ")
            append(filterComplex)
            append("-vcodec libx264 ")
            if(!DevelopUtil.isEncodingQualityOptimization(context)) append("-crf 28 ")
            append("-an ")
            append("-y ")
            append("\"$dst\"")
        }

        return FFmpeg.execute(command.toString())
    }

    private fun makeThemeImage(context:Context, local:FThemeLocal) : Int{
        val path = Uri.fromFile(local.getThemeProvider(context).file).path ?: return RESULT_FAILED
        val frame = getFrame(path) ?: return RESULT_FAILED

        saveImage(local.getThumbnailProvider(context).file, frame, 360)
        saveImage(local.getPreloadProvider(context).file, frame, 720)

        return RESULT_OK
    }

    private fun getFrame(src: String) : Bitmap? {
        val mr = MediaMetadataRetriever()
        mr.setDataSource(src)
        val frame = mr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        mr.release()
        return frame
    }

    private fun saveImage(file:File, bitmap:Bitmap, width:Int){
        val factor = width.toFloat() / bitmap.width
        val h = (bitmap.height * factor).toInt()
        val out = FileOutputStream(file)

        Bitmap.createScaledBitmap(bitmap, width, h, true)
            .compress(Bitmap.CompressFormat.JPEG, 70, out)
        out.close()
    }

    companion object{
        const val RESULT_OK = 0
        const val RESULT_CANCEL = 255
        const val RESULT_FAILED = 1
    }
}