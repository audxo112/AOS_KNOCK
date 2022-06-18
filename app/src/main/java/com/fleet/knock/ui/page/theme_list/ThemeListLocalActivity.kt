package com.fleet.knock.ui.page.theme_list

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityThemeListLocalBinding
import com.fleet.knock.databinding.ViewThemeListLocalItemBinding
import com.fleet.knock.info.diff.FThemeLocalDiffCallback
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.ui.dialog.DeleteThemeDialog
import com.fleet.knock.ui.page.preview.PreviewLocalActivity
import com.fleet.knock.utils.StorageUtil
import com.fleet.knock.utils.recycler.BaseViewHolder

class ThemeListLocalActivity : AppCompatActivity(){
    private lateinit var binding : ActivityThemeListLocalBinding
    private val viewModel: ThemeListLocalViewModel by viewModels()

    private val deleteThemeDialog by lazy{
        DeleteThemeDialog()
    }

    private var selectedLocalThemeId:Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_theme_list_local)
        binding.apply{
            lifecycleOwner = this@ThemeListLocalActivity
            vm = viewModel

            back.setOnClickListener {
                onBackPressed()
            }

            themeList.adapter = LocalThemeAdapter()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_PERMISSION_LOCAL_THEME && StorageUtil.isPermission(applicationContext)){
            gotoLocalPreview(selectedLocalThemeId)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun gotoLocalPreview(themeId: Long?) {
        themeId ?: return
        selectedLocalThemeId = null

        if(StorageUtil.isPermission(applicationContext)){
            startActivity(Intent(this, PreviewLocalActivity::class.java).apply {
                putExtra(PreviewLocalActivity.EXTRA_THEME_ID, themeId)
            })
            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_not_move)
        }
        else{
            selectedLocalThemeId = themeId
            StorageUtil.requestPermission(this, REQUEST_PERMISSION_LOCAL_THEME)
        }
    }

    private fun showDeleteThemeDialog(callback:()->Unit){
        if(!deleteThemeDialog.isAdded) {
            deleteThemeDialog.setOnThemeDeleteListener(callback)
            deleteThemeDialog.show(supportFragmentManager, "DeleteTheme")
        }
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    inner class LocalThemeAdapter : ListAdapter<FThemeLocal, LocalThemeHolder>(FThemeLocalDiffCallback()){
        private val themeHeight = resources.displayMetrics.let { dm ->
            ((dm.widthPixels - dpToPixel(24)) / 3) * dm.heightPixels / dm.widthPixels
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalThemeHolder {
            return LocalThemeHolder(parent, themeHeight)
        }

        override fun onBindViewHolder(holder: LocalThemeHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class LocalThemeHolder(parent:ViewGroup, height:Int) : BaseViewHolder<FThemeLocal>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_theme_list_local_item, parent, false).apply{
                layoutParams.height = height
            }
    ){
        private var binding : ViewThemeListLocalItemBinding? = null

        override fun bind(data: FThemeLocal?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = this@ThemeListLocalActivity
                theme = data
            }

            binding?.themeThumbnailEffect?.setOnClickListener {
                val themeId = data?.id ?: return@setOnClickListener

                gotoLocalPreview(themeId)
            }

            binding?.themeThumbnailEffect?.setOnLongClickListener {
                val themeId = data?.id ?: return@setOnLongClickListener false

                showDeleteThemeDialog {
                    viewModel.deleteItem(themeId)
                }
                return@setOnLongClickListener true
            }
        }
    }

    companion object{
        const val REQUEST_PERMISSION_LOCAL_THEME = 10001
    }
}