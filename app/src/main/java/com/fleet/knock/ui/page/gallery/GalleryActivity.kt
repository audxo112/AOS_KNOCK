package com.fleet.knock.ui.page.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityGalleryBinding
import com.fleet.knock.info.gallery.GifImage
import com.fleet.knock.info.gallery.Video
import com.fleet.knock.ui.page.editor.EditorActivity
import com.fleet.knock.ui.toast.SwapToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.FileNotFoundException

class GalleryActivity : AppCompatActivity(), GotoEditorDelegate{
    private lateinit var binding:ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)
        binding.apply{
            lifecycleOwner = this@GalleryActivity

            close.setOnClickListener {
                onBackPressed()
            }

            tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    setStyle(tab, R.style.TabUnselectedText)
                }
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    setStyle(tab, R.style.TabSelectedText)
                }

                private fun setStyle(tab:TabLayout.Tab?, style:Int){
                    val views = arrayListOf<View>()
                    tab?.view?.findViewsWithText(views, tab.text, View.FIND_VIEWS_WITH_TEXT)
                    views.forEach{view->
                        if(view is TextView) {
                            TextViewCompat.setTextAppearance(view, style)
                        }
                    }
                }
            })
        }

        binding.pager.apply{
            offscreenPageLimit = 1
            adapter = GalleryAdapter()

            TabLayoutMediator(binding.tabLayout, this){tab, position ->
                tab.text = when(position){
                    PAGE_VIDEO_POS -> getString(R.string.activity_gallery_page_video)
                    else -> getString(R.string.activity_gallery_page_gif)
                }
            }.attach()
        }
    }

    override fun onBackPressed() {
        if(!isFinishing) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("GalleryActivity", "request : ${requestCode} result : ${resultCode} data : ${data}")

        if(requestCode == REQUEST_EDIT_THEME){
            when(resultCode){
                Activity.RESULT_OK ->{
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                RESULT_FINISH ->{
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    override fun gotoEditor(gif: GifImage?) {
        gif ?: return

//        val uri =
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) gif.contentUri
//            else gif.getUri(applicationContext) ?: return

        val path = try {
            gif.getUri(applicationContext)?.path
        }
        catch (e:FileNotFoundException){
            SwapToast.makeText(applicationContext, R.string.activity_gallery_file_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        if(path == "") {
            SwapToast.makeText(applicationContext, R.string.activity_gallery_load_failed, Toast.LENGTH_SHORT).show()
        }
        else {
            val intent = Intent(this, EditorActivity::class.java).apply {
                putExtra(EditorActivity.EXTRA_TYPE, EditorActivity.TYPE_GIF)
                putExtra(EditorActivity.EXTRA_URI, path)
                putExtra(EditorActivity.EXTRA_DURATION, gif.getDuration(applicationContext))
                putExtra(EditorActivity.EXTRA_DEFAULT_RESIZE_MODE, gif.resizeMode)
            }
            startActivityForResult(intent, REQUEST_EDIT_THEME)
        }
    }

    override fun gotoEditor(video: Video?) {
        video ?: return
//        val path =
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) video.contentUri.toString()
//            else video.getUri(applicationContext)?.path ?: return

        val path = try {
            video.getUri(applicationContext)?.path
        }
        catch (e:FileNotFoundException){
            SwapToast.makeText(applicationContext, R.string.activity_gallery_file_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        if(path == "") {
            SwapToast.makeText(applicationContext, R.string.activity_gallery_load_failed, Toast.LENGTH_SHORT).show()
        }
        else {
            val intent = Intent(this, EditorActivity::class.java).apply {
                putExtra(EditorActivity.EXTRA_TYPE, EditorActivity.TYPE_VIDEO)
                putExtra(EditorActivity.EXTRA_URI, path)
                putExtra(EditorActivity.EXTRA_DURATION, video.getDuration(applicationContext))
                putExtra(EditorActivity.EXTRA_DEFAULT_RESIZE_MODE, video.getResizeMode(applicationContext))
            }
            startActivityForResult(intent, REQUEST_EDIT_THEME)
        }
    }


    inner class GalleryAdapter : FragmentStateAdapter(this){
        override fun createFragment(position: Int): Fragment {
            return if(position == PAGE_VIDEO_POS){
                GalleryVideoFragment()
            }
            else{
                GalleryGifFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }

    companion object{
        const val REQUEST_EDIT_THEME = 2010

        const val RESULT_FINISH = 1

        const val PAGE_VIDEO_POS = 0
        const val PAGE_GIF_POS = 1
    }
}