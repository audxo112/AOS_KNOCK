package com.fleet.knock.ui.page.promotion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityPromotionBinding
import com.fleet.knock.databinding.ViewPromotionProjectBinding
import com.fleet.knock.databinding.ViewPromotionThemeBinding
import com.fleet.knock.info.promotion.FProject
import com.fleet.knock.info.theme.FThemeEntity
import com.fleet.knock.ui.page.preview.PreviewPublicActivity
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SpaceHorizontalDivider
import com.fleet.knock.utils.recycler.SpaceVerticalDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PromotionActivity : AppCompatActivity(){
    private lateinit var binding:ActivityPromotionBinding
    private val viewModel by lazy{
        PromotionViewModel.new(this, promotionId)
    }

    private val promotionId
        get() = intent.extras?.getString(EXTRA_PROMOTION_ID) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotion)
        binding.apply{
            lifecycleOwner = this@PromotionActivity
            vm = viewModel

            back.setOnClickListener {
                onBackPressed()
            }

            mainEffect.setOnClickListener {
                gotoLink()
            }
        }

        binding.projectList.apply{
            adapter = ProjectAdapter()
            addItemDecoration(
                SpaceVerticalDivider(
                    dpToPixel(8),
                    dpToPixel(40),
                    dpToPixel(40)
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_SHOW_THEME_LIST){
            val projectId = data?.extras?.getString(PreviewPublicActivity.EXTRA_PROJECT_ID, null)
            val themeId = data?.extras?.getString(PreviewPublicActivity.EXTRA_THEME_ID, null)
            scrollTo(projectId, themeId)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun scrollTo(projectId:String?, themeId:String?){
        projectId ?: return
        themeId ?: return

        val projectAdapter = binding.projectList.adapter?.let{
            if(it is ProjectAdapter) it
            else null
        } ?: return

        val projectPos = projectAdapter.getProjectPosById(projectId)
        if(projectPos == -1) return

        val projectList = binding.projectList.getChildAt(projectPos) ?: return

        binding.scrollView.scrollTo(0, projectList.top)

        val themeList = projectList.findViewById<RecyclerView>(R.id.project_theme_list) ?: return

        val themeAdapter = themeList.adapter?.let{
            if(it is ThemeAdapter) it
            else null
        } ?: return

        val themePos = themeAdapter.getThemePosById(themeId)
        if(themePos == -1) return

        val themeManager = themeList.layoutManager?.let{
            if(it is LinearLayoutManager) it
            else null
        } ?: return

        if(themePos >= themeManager.findFirstVisibleItemPosition() &&
            themePos <= themeManager.findLastVisibleItemPosition()) return

        themeList.scrollToPosition(themePos)
    }

    private fun gotoPublicPreview(promotionId:String, themeId: String) {
        startActivityForResult(Intent(this, PreviewPublicActivity::class.java).apply {
            putExtra(PreviewPublicActivity.EXTRA_PAGE, PreviewPublicActivity.PAGE_PROJECT)
            putExtra(PreviewPublicActivity.EXTRA_PROMOTION_ID, promotionId)
            putExtra(PreviewPublicActivity.EXTRA_THEME_ID, themeId)
        }, REQUEST_SHOW_THEME_LIST)
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_not_move)
    }

    private fun gotoLink(){
        val link = viewModel.mainLink
        if(link == "") return

        viewModel.logEnterPromotionLink()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        if(intent.resolveActivity(packageManager) != null)
            startActivity(intent)
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    inner class ProjectAdapter() : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return ProjectHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is ProjectHolder && list[position] is FProject)
                holder.bind(list[position] as FProject)
        }

        fun getProjectPosById(projectId:String) : Int{
            return list.indexOfFirst {
                if(it is FProject)
                    it.projectId == projectId
                else
                    false
            }
        }
    }

    inner class ProjectHolder(parent:ViewGroup) : BaseViewHolder<FProject>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_promotion_project, parent, false)
    ) {
        private var binding:ViewPromotionProjectBinding? = null

        init {
            val themeList: RecyclerView = view.findViewById(R.id.project_theme_list)
            themeList.adapter = ThemeAdapter()
            themeList.addItemDecoration(
                SpaceHorizontalDivider(
                    dpToPixel(21),
                    dpToPixel(21),
                    dpToPixel(8)
                )
            )
        }

        override fun bind(data: FProject?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = this@PromotionActivity
                project = data
            }
        }
    }

    inner class ThemeAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return ThemeHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is ThemeHolder && list[position] is FThemeEntity)
                holder.bind(list[position] as FThemeEntity)
        }

        fun getThemePosById(themeId:String) : Int{
            return list.indexOfFirst {
                if(it is FThemeEntity)
                    it.id == themeId
                else
                    false
            }
        }
    }

    inner class ThemeHolder(parent:ViewGroup) : BaseViewHolder<FThemeEntity>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_promotion_theme, parent, false)
    ){
        private var binding:ViewPromotionThemeBinding? = null

        private val viewModel:ProjectThemeHolderViewModel by lazy{
            ViewModelProvider(this@PromotionActivity).get(
                toString(),
                ProjectThemeHolderViewModel::class.java
            )
        }

        private val job = Job()

        override fun bind(data: FThemeEntity?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = this@PromotionActivity
                theme = data

                themeThumbnailEffect.setOnClickListener {
                    val theme = data ?: return@setOnClickListener
                    gotoPublicPreview(theme.promotionId, theme.id)
                }
            }

            bindThumbnail(data)
            downloadPreload(data)
        }

        private fun bindThumbnail(theme:FThemeEntity?){
            theme?:return
            val img = binding?.themeThumbnail ?: return
            img.setImageDrawable(null)

            lifecycleScope.launch {
                val drawable = loadThumbnail(theme)
                if(theme.id != binding?.theme?.id){
                    bindThumbnail(binding?.theme)
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

        private suspend fun loadThumbnail(theme:FThemeEntity?) = withContext(Dispatchers.IO + job){
            theme ?: return@withContext null
            if(!theme.existThumbnail)
                viewModel.thumbnailDownload(theme)
            theme.getThumbnail(application)
        }

        private fun downloadPreload(theme:FThemeEntity?) = lifecycleScope.launch(Dispatchers.IO + job){
            theme?: return@launch
            if(!theme.existPreload)
                viewModel.preloadDownload(theme)
        }
    }

    companion object{
        const val REQUEST_SHOW_THEME_LIST = 2011

        const val EXTRA_PROMOTION_ID = "Extra.PromotionId"
    }
}