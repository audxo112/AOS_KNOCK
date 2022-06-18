package com.fleet.knock.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.io.*

object StorageUtil {
    const val REQUEST_PICK_IMAGE = 2001
    const val REQUEST_PICK_THEME = 2002

    const val REQUEST_EXTERNAL_STORAGE_PERMISSION = 10000

    fun isPermission(context:Context) : Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun requestPermission(activity: Activity, requestCode:Int = REQUEST_EXTERNAL_STORAGE_PERMISSION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), requestCode
            )
        }
    }

    fun requestPermission(fragment:Fragment, requestCode: Int = REQUEST_EXTERNAL_STORAGE_PERMISSION){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragment.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), requestCode
            )
        }
    }

    fun existVideo(resolver:ContentResolver, dst:String) : Boolean{
        val projection = arrayOf(MediaStore.Video.Media._ID)

        val selection = "${MediaStore.Video.Media.DISPLAY_NAME}=?"
        val selectionArg = arrayOf(
            dst
        )

        val cursor = resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArg,
            null,
            null
        )
        cursor ?: return false

        val exist = cursor.count > 0
        cursor.close()

        return exist
    }

    fun saveVideo(resolver: ContentResolver, src:File?, fileName:String) : Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            saveFileApi10(resolver, src, MIME_TYPE_MP4, "${Environment.DIRECTORY_DCIM}/KNOCK", fileName)
        }
        else{
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "KNOCK")
            if(!dir.exists())
                dir.mkdirs()

            saveFile(resolver, src, MIME_TYPE_MP4, dir.absolutePath, fileName)
        }
    }

    private fun saveFile(input: FileInputStream, output: FileOutputStream) : Boolean{
        try {
            val buffer = ByteArray(1024)
            var bSize = input.read(buffer)
            while (bSize > 0) {
                output.write(buffer)
                bSize = input.read(buffer)
            }
        }
        catch(e:Exception){
            e.printStackTrace()
            return false
        }
        finally {
            input.close()
            output.close()
        }
        return true
    }

    private fun saveFile(resolver: ContentResolver, src:File?, mimeType:String, dstFileDir:String, dstFileName:String) : Boolean{
        src?:return false

        val file = File(dstFileDir, dstFileName)

        val values = ContentValues().apply{
            put(MediaStore.Video.Media.DISPLAY_NAME, dstFileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.DATA, file.absolutePath)
        }

        resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return false

        return saveFile(FileInputStream(src), FileOutputStream(file))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileApi10(resolver: ContentResolver, src:File?, mimeType:String, dstFileDir:String, dstFileName:String) : Boolean{
        src?:return false
        val values = ContentValues().apply{
            put(MediaStore.Video.Media.RELATIVE_PATH, dstFileDir)
            put(MediaStore.Video.Media.DISPLAY_NAME, dstFileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val item = resolver.insert(collection, values) ?: return false

        resolver.openFileDescriptor(item, "w", null)?.use{
            FileOutputStream(it.fileDescriptor).use { output->
                if(!saveFile(FileInputStream(src), output))
                    return false
            }
        }?: return false

        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(item, values, null, null)

        return true
    }

    fun deleteFile(resolver: ContentResolver, fileName:String){
        val projection = arrayOf(MediaStore.Video.Media._ID)

        val selection = "${MediaStore.Video.Media.DISPLAY_NAME}=?"
        val selectionArg = arrayOf(
            fileName
        )

        val cursor = resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArg,
            null
        )
        cursor ?: return

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        while(cursor.moveToNext()){
            val id = cursor.getLong(idColumn)
            val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
            resolver.delete(uri, null, null)
        }

        cursor.close()
    }

    fun openGalleryWithTheme(activity:Activity) {
        if (isPermission(activity)) {
            activity.startActivityForResult(createPickIntent(), REQUEST_PICK_THEME)
        } else {
            requestPermission(activity)
        }
    }

    fun openGalleryWithTheme(fragment:Fragment){
        fragment.context ?: return
        if (isPermission(fragment.requireContext())) {
            fragment.startActivityForResult(createPickIntent(), REQUEST_PICK_THEME)
        } else {
            requestPermission(fragment)
        }
    }

    private fun createPickIntent() = Intent.createChooser(Intent(Intent.ACTION_PICK).apply {
        type = "*/*"
    }, "GIF or 영상 선택")

    private const val MIME_TYPE_MP4 = "video/mp4"
}