package com.fleet.knock.ui.page.theme_list

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityThemeListRecentBinding
import com.fleet.knock.databinding.ViewThemeListRecentItemBinding
import com.fleet.knock.info.diff.FThemeDiffCallback
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.ui.page.preview.PreviewPublicActivity
import com.fleet.knock.utils.recycler.BaseViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemeListRecentActivity : AppCompatActivity(){
    private lateinit var binding : ActivityThemeListRecentBinding
    private val viewModel : ThemeListRecentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_theme_list_recent)
        binding.apply{
            lifecycleOwner = this@ThemeListRecentActivity
            vm = viewModel

            back.setOnClickListener {
                onBackPressed()
            }

            themeList.adapter = RecentThemeAdapter()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun gotoPreview(themeId:String?){
        themeId ?: return

        startActivityForResult(Intent(this, PreviewPublicActivity::class.java).apply{
            putExtra(PreviewPublicActivity.EXTRA_PAGE, PreviewPublicActivity.PAGE_RECENT)
            putExtra(PreviewPublicActivity.EXTRA_THEME_ID, themeId)
        }, REQUEST_SHOW_THEME_LIST)
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_not_move)
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    inner class RecentThemeAdapter : ListAdapter<FTheme, RecentThemeHolder>(FThemeDiffCallback()){
        private val themeHeight = resources.displayMetrics.let{ dm ->
            ((dm.widthPixels - dpToPixel(24)) / 3) * dm.heightPixels / dm.widthPixels
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentThemeHolder {
            return RecentThemeHolder(parent, themeHeight)
        }

        override fun onBindViewHolder(holder: RecentThemeHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class RecentThemeHolder(parent: ViewGroup, height:Int) : BaseViewHolder<FTheme>(
        LayoutInflater.from(parent.context).inflate(R.layout.view_theme_list_recent_item, parent, false).apply{
            layoutParams.height = height
        }
    ){
        private var hBinding : ViewThemeListRecentItemBinding? = null

        private val job = Job()

        override fun bind(data: FTheme?) {
            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply{
                lifecycleOwner = this@ThemeListRecentActivity
                theme = data
            }

            hBinding?.themeThumbnailEffect?.setOnClickListener {
                val themeId = data?.id ?: return@setOnClickListener

                gotoPreview(themeId)
            }

            bindThumbnail(data)
            downloadPreload(data)
        }

        private fun bindThumbnail(theme:FTheme?){
            theme?:return
            val img = hBinding?.themeThumbnail ?: return
            img.setImageDrawable(null)

            lifecycleScope.launch {
                val drawable = loadThumbnail(theme)
                if(theme.id != hBinding?.theme?.id){
                    bindThumbnail(hBinding?.theme)
                    return@launch
                }

                try {
                    Glide.with(img)
                        .load(drawable)
                        .into(img)
                } catch (e:IllegalStateException){
                    return@launch
                } catch (e:IllegalArgumentException){
                    return@launch
                }
            }
        }

        private suspend fun loadThumbnail(theme: FTheme?) = withContext(Dispatchers.IO + job){
            theme ?: return@withContext null
            if(!theme.existThumbnail)
                viewModel.thumbnailDownload(theme)
            theme.getThumbnail(application)
        }

        private fun downloadPreload(theme:FTheme?) = lifecycleScope.launch(Dispatchers.IO + job){
            theme?: return@launch
            if(!theme.existPreload)
                viewModel.preloadDownload(theme)
        }
    }

    companion object{
        const val REQUEST_SHOW_THEME_LIST = 2000
    }
}