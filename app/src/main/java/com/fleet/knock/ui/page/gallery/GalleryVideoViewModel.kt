package com.fleet.knock.ui.page.gallery

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.ContentResolverCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.gallery.Video
import com.fleet.knock.info.gallery.VideoBundle
import com.fleet.knock.utils.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GalleryVideoViewModel(application: Application) : BaseViewModel(application){
    val videoList = MutableLiveData<List<VideoBundle>>()
    private val existVideo = Transformations.map(videoList) {
        it.isNotEmpty()
    }

    private val isLoading = MutableLiveData(false)
    val visibilityVideo = MediatorLiveData<Int>().apply{
        addSource(isLoading){
            visibilityVideo(existVideo.value, it)
        }
        addSource(existVideo){
            visibilityVideo(it, isLoading.value)
        }
    }

    private fun visibilityVideo(existVideo:Boolean?, isLoading:Boolean?){
        if(existVideo == null || isLoading == null){
            visibilityVideo.value = View.INVISIBLE
            return
        }

        visibilityVideo.value = if(existVideo && isLoading) View.VISIBLE
        else View.INVISIBLE
    }

    val visibilityVideoEmpty = MediatorLiveData<Int>().apply{
        addSource(isLoading){
            visibilityVideoEmpty(existVideo.value, it)
        }
        addSource(existVideo){
            visibilityVideoEmpty(it, isLoading.value)
        }
    }

    private fun visibilityVideoEmpty(existVideo:Boolean?, isLoading:Boolean?){
        if(existVideo == null || isLoading == null){
            visibilityVideoEmpty.value = View.INVISIBLE
            return
        }

        visibilityVideoEmpty.value = if(!existVideo && isLoading) View.VISIBLE
        else View.INVISIBLE
    }

    init{
        viewModelScope.launch(Dispatchers.IO) {
            loadVideo()
        }
    }

    private fun getVideoQuery() : Cursor?{
        val projection = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION)
        }
        else {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT)
        }
        val selection = "${MediaStore.Video.Media.MIME_TYPE} in (?,?,?,?)"
        val selectionArg = arrayOf(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("webm"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("mkv"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("ts")
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        return ContentResolverCompat.query(
            getApplication<Application>().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArg,
            sortOrder,
            null)
    }

    private fun getVideoDuration(cursor: Cursor) : Long?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
        } else {
            null
        }
    }

    private suspend fun loadVideo() {
        val cursor = getVideoQuery()
        cursor ?: return

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)

        val dateMap = TreeMap<String, ArrayList<Video>>(Collections.reverseOrder())
        val list = ArrayList<VideoBundle>()

        while(cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id
            )
            val duration = getVideoDuration(cursor)

            val ratio = cursor.getInt(heightColumn).toFloat() / cursor.getInt(widthColumn)

            val dateAdded = cursor.getLong(dateAddedColumn) * 1000
            val dateStr = format.format(dateAdded)

            if(!dateMap.containsKey(dateStr)){
                dateMap[dateStr] = arrayListOf()
            }

            dateMap[dateStr]?.add(Video(id,
                null,
                contentUri,
                null,
                duration,
                ratio))
        }
        cursor.close()

        dateMap.forEach{entry ->
            list.add(VideoBundle(format.parse(entry.key)?.time ?: 0L, entry.value))
        }

        withContext(Dispatchers.Main){
            videoList.value = list
            isLoading.value = true
        }
    }
}