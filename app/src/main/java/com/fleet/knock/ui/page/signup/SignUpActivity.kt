package com.fleet.knock.ui.page.signup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface

import com.fleet.knock.R
import com.fleet.knock.databinding.ActivitySignupBinding
import com.fleet.knock.utils.UriUtil
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class SignUpActivity : AppCompatActivity(){
    lateinit var binding: ActivitySignupBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 테스트 후 삭제 요망
        FirebaseFirestore.getInstance().clearPersistence()

        binding = DataBindingUtil.setContentView<ActivitySignupBinding>(this, R.layout.activity_signup).apply{
            lifecycleOwner = this@SignUpActivity
            vm = signUpViewModel
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.avatar.setOnClickListener {
            loadAvatarFromGallery()
        }

        binding.complete.setOnClickListener {
            signUp()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_PICK_IMAGE &&
            resultCode == Activity.RESULT_OK ){

            signUpViewModel.avatar.value = data?.data?.let{uri->
                UriUtil.getPath(application, uri)?.let{path->
                    resizeAndCropBitmap(File(path), 128)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION && checkPermission()){
            openGallery()
        }
    }

    private fun resizeAndCropBitmap(file:File, maxDP:Int) : Bitmap{
        val src:Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val dp = dpToPixel(maxDP)
        val size = if(src.width <= src.height) src.width
        else src.height
        val exif = ExifInterface(file.absolutePath)
        val matrix = Matrix().apply{
            postRotate(exif.rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(src, (src.width - size) / 2, (src.height - size) / 2, size, size, matrix, true).let {
            Bitmap.createScaledBitmap(it, dp, dp,true)
        }
    }

    private fun checkPermission() : Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        return  true
    }

    private fun requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    private fun openGallery(){
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            startActivityForResult(this, REQUEST_PICK_IMAGE)
        }
    }

    private fun loadAvatarFromGallery(){
        if(checkPermission()) {
            openGallery()
        }
        else{
            requestPermission()
        }
    }

    private fun signUp(){
        if(!signUpViewModel.signUp {
            Intent(this, SignUpCompleteActivity::class.java).also{
                startActivity(it)
                finish()
            }
        }){
            Toast.makeText(this, "회원가입 실패!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPixel(dp: Int): Int {
        return Resources.getSystem().displayMetrics.let{
            (dp * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }

    companion object{
        const val REQUEST_PICK_IMAGE = 1000

        const val REQUEST_EXTERNAL_STORAGE_PERMISSION = 10000
    }
}