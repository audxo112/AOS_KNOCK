package com.fleet.knock.info.provider

import android.content.Context
import android.net.Uri
import java.io.File

open class FileProvider(context: Context,
                   vararg paths:String){
    val file: File
    val gsLink:String

    val uri: Uri
        get() = Uri.fromFile(file)

    val isExistFile:Boolean
        get() = file.exists()

    fun isValidFile(storageFileSize:Long) = isExistFile && file.length() == storageFileSize

    fun deleteFile(){
        if(file.exists()) {
            file.delete()
        }
    }

    init{
        paths.map { it.trim().replace(" ", "_") }
        paths.filter{ it != "" }

        var file = context.getDir("", Context.MODE_PRIVATE)
        val gsLink = StringBuilder()

        for(i in paths.indices){
            gsLink.append("/").append(paths[i])
            file = File(file, paths[i])

            if(i < paths.size - 1 && !file.exists())
                file.mkdirs()
        }
        this.file = file
        this.gsLink = gsLink.toString()
    }
}