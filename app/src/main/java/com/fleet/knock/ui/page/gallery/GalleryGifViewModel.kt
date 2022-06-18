package com.fleet.knock.ui.page.gallery

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.ContentResolverCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.fleet.knock.info.gallery.GifImage
import com.fleet.knock.info.gallery.GifImageBundle
import com.fleet.knock.utils.viewmodel.BaseViewModel
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GalleryGifViewModel(application:Application) : BaseViewModel(application) {

    val gifList = MutableLiveData<List<GifImageBundle>>()
    private val existGif = Transformations.map(gifList){
        it.isNotEmpty()
    }

    private val isLoading = MutableLiveData(false)
    val visibilityGif = MediatorLiveData<Int>().apply{
        addSource(isLoading){
            visibilityGif(existGif.value, it)
        }
        addSource(existGif){
            visibilityGif(it, isLoading.value)
        }
    }

    private fun visibilityGif(existGif:Boolean?, isLoading:Boolean?){
        val e = existGif ?: return
        val l = isLoading ?: return

        visibilityGif.value = if(e && l) View.VISIBLE
        else View.INVISIBLE
    }

    val visibilityGifEmpty = MediatorLiveData<Int>().apply{
        addSource(isLoading){
            visibilityGifEmpty(existGif.value, it)
        }
        addSource(existGif){
            visibilityGifEmpty(it, isLoading.value)
        }
    }

    private fun visibilityGifEmpty(existGif:Boolean?, isLoading:Boolean?){
        val e = existGif ?: return
        val l = isLoading ?: return

        visibilityGifEmpty.value = if(!e && l) View.VISIBLE
        else View.INVISIBLE
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadGif()
        }
    }

    private fun getGifQuery(): Cursor? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT)

        val selection = "${MediaStore.Images.Media.MIME_TYPE}=?"
        val selectionArg = arrayOf(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif")
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        return ContentResolverCompat.query(
            getApplication<Application>().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArg,
            sortOrder,
            null
        )
    }

    private fun getGifResizeMode(ratio:Float) : Int {
        return if(ratio >= 1.3) AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        else AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    private suspend fun loadGif() {
        val cursor = getGifQuery()
        cursor ?: return

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)

        val dateMap = TreeMap<String, ArrayList<GifImage>>(Collections.reverseOrder())
        val list = ArrayList<GifImageBundle>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val ratio = cursor.getInt(heightColumn).toFloat() / cursor.getInt(widthColumn)

            val dateAdded = cursor.getLong(dateAddedColumn) * 1000
            val dateStr = format.format(dateAdded)

            if(!dateMap.containsKey(dateStr)){
                dateMap[dateStr] = arrayListOf()
            }

            dateMap[dateStr]?.add(GifImage(id,
                null,
                contentUri,
                null,
                null,
                getGifResizeMode(ratio)))
        }
        cursor.close()

        dateMap.forEach{entry->
            list.add(GifImageBundle(format.parse(entry.key)?.time ?: 0L, entry.value))
        }

        withContext(Dispatchers.Main){
            gifList.value = list
            isLoading.value = true
        }
    }
}