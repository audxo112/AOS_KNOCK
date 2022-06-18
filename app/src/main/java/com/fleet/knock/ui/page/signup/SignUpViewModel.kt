package com.fleet.knock.ui.page.signup

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R
import com.fleet.knock.info.user.User
import com.fleet.knock.utils.viewmodel.BaseViewModel
import com.fleet.knock.utils.viewmodel.CheckTextViewModelHolder
import com.fleet.knock.utils.viewmodel.RemainTextViewModelHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class SignUpViewModel(application: Application) : BaseViewModel(application){
    val avatar = MutableLiveData<Bitmap?>(null)
    val avatarBitmap = Transformations.map(avatar){
        it?: BitmapFactory.decodeResource(application.resources, R.drawable.default_user_avatar)
    }

    val id = CheckTextViewModelHolder(
        4,
        COLLECTION_USERS,
        FIELD_ID,
        "존재하는 아이디 입니다."
    )
    val nickname = CheckTextViewModelHolder(
        2,
        COLLECTION_USERS,
        FIELD_NICKNAME,
        "존재하는 네임 입니다."
    )

    val introduce = RemainTextViewModelHolder(40)

    private val complete = MediatorLiveData<Boolean>().apply{
        addSource(id.check){
            if(nickname.check.value != null)
                value = it && (nickname.check.value == true)
        }
        addSource(nickname.check) {
            if(id.check.value != null)
                value = it && (id.check.value == true)
        }
    }
    val completeColorRes = Transformations.map(complete){
        if(it) R.color.colorTextPrimary else R.color.colorTextGray
    }
    val completeBackgroundRes = Transformations.map(complete){
        if(it) R.drawable.ripple_primary_a30_circle_btn else 0
    }
    private val uploading = MutableLiveData<Boolean>(false)
    val uploadingVisibility = Transformations.map(uploading){
        if(it) View.VISIBLE else View.INVISIBLE
    }
    private var uploadUserData = false
    private var uploadAvatar = false

    private fun uploadAvatar(user: FirebaseUser, callback:()->Unit){
        val stream = ByteArrayOutputStream()
        avatar.value?.compress(Bitmap.CompressFormat.JPEG, 75, stream)

        FirebaseStorage.getInstance().reference
            .child(DIR_USERS)
            .child(DIR_AVATAR)
            .child("${user.uid}.jpg")
            .putBytes(stream.toByteArray())
            .addOnSuccessListener{
                uploadAvatar = true
                completeUpload(callback)
            }
            .addOnFailureListener{
                uploading.value = false
            }
    }

    private fun upload(user: FirebaseUser, callback:()->Unit){
        val cal = Calendar.getInstance()
        val updateTime = cal.time

        cal.add(Calendar.YEAR, 100)
        val uploadStopPeriod = cal.time

        val userMap = hashMapOf(
            "user_id" to id.text.value as String,
            "nickname" to nickname.text.value as String,
            "introduce" to introduce.text.value as String,
            "grade" to User.GRADE_NORMAL,
            "update_avatar_time" to updateTime,
            "upload_stop_period" to uploadStopPeriod,
            "update_time" to updateTime,
            "exist_avatar" to false,
            "avatar_ext" to ""
        )

        if(avatar.value == null){
            uploadAvatar = true
        }

        avatar.value?.let{
            userMap.putAll(hashMapOf(
                "exist_avatar" to true,
                "avatar_ext" to "jpg"
            ))
            uploadAvatar(user, callback)
        }

        FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
            .document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                uploadUserData = true
                completeUpload(callback)
            }
            .addOnFailureListener{
                uploading.value = false
            }
    }

    private fun completeUpload(callback:()->Unit){
        if(uploadUserData && uploadAvatar){
            callback()
        }
    }

    fun signUp(callback:()->Unit) : Boolean{
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        return complete.value?.also{
            uploading.value = it
            if(it) {
                upload(user, callback)
            }
        }?: false
    }

    companion object{
        const val DIR_USERS = "users"
        const val DIR_AVATAR = "avatar"

        const val COLLECTION_USERS = "users"
        const val FIELD_ID = "userId"
        const val FIELD_NICKNAME = "nickname"
    }
}