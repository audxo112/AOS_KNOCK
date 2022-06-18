package com.fleet.knock.ui.page.preview

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityPreviewBinding
import com.fleet.knock.databinding.ViewPreviewPublicBinding
import com.fleet.knock.databinding.ViewPreviewTagBinding
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.info.user.User
import com.fleet.knock.ui.dialog.ApplyDialog
import com.fleet.knock.ui.dialog.RemoveWallpaperDialog
import com.fleet.knock.ui.dialog.TutorialPreviewSwipeDialog
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.WallpaperUtil
import com.fleet.knock.utils.admob.BaseADUtil
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.ExoPlayerViewHolder
import com.fleet.knock.utils.recycler.ExoPlayerPagerHelper
import com.fleet.knock.utils.tutorial.TutorialPreviewSwipe
import com.fleet.knock.utils.tutorial.TutorialUtil
import com.google.android.flexbox.*
import kotlinx.coroutines.*

class PreviewPublicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    private val viewModel by lazy{
        PreviewPublicViewModel.new(this, page, promotionId)
    }

    private val helper by lazy{
        ExoPlayerPagerHelper(this)
    }

    private val applyDialog by lazy{
        ApplyDialog()
    }

    private val removeWallpaperDialog by lazy{
        RemoveWallpaperDialog().apply{
            setOnRemoveWallpaper {
                WallpaperUtil.removeWallpaper(this@PreviewPublicActivity,
                onProgress = {
                    viewModel.isLoading.value = it
                },
                onComplete = {
                    viewModel.removeSelectTheme()
                })
            }
        }
    }

    private val tutorialPreviewDialog by lazy{
        TutorialPreviewSwipeDialog()
    }

    private val page
        get() = intent.extras?.getString(EXTRA_PAGE) ?: PAGE_ALL

    private val promotionId
        get() = intent.extras?.getString(EXTRA_PROMOTION_ID) ?: ""

    private val selectedThemeId
        get() = intent.extras?.getString(EXTRA_THEME_ID) ?: ""

    private val currentTheme:FTheme?
        get(){
            val adapter = binding.pager.adapter?.let{
                if(it is PreviewAdapter) it
                else null
            } ?: return null

            val currentPos = helper.currentPos.value ?: return null

            return adapter.getThemeByPos(currentPos)
        }

    private var isApplyWallpaper = false

    private val scene = PreviewScene()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview)
        binding.apply{
            lifecycleOwner = this@PreviewPublicActivity
            vm = viewModel

            homeScreen.setOnClickListener {
                gotoPreview(PreviewPublicViewModel.PAGE_HOME_PREVIEW)
            }

            lockScreen.setOnClickListener {
                gotoPreview(PreviewPublicViewModel.PAGE_LOCK_PREVIEW)
            }
        }

        helper.apply{
            pager = binding.pager
            setAdapter(PreviewAdapter())
        }

        lifecycleScope.launch {
            val themeList = withContext(Dispatchers.IO){
                viewModel.getThemeList()
            }

            helper.add(themeList, themeList.indexOfFirst {
                it.id == selectedThemeId
            })

            requestSwipeTutorial()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == WallpaperUtil.REQUEST_SET_THEME){
            isApplyWallpaper = false
            if(WallpaperUtil.isSetWallpaper(this)){
                viewModel.logSetWallpaper()

                appliedTheme()
            }
            else{
                viewModel.restoreSelectTheme()
            }
        }
        else if(requestCode == WallpaperUtil.REQUEST_SET_WALLPAPER){
            isApplyWallpaper = false
            viewModel.logResetWallpaper()
        }
    }

    override fun onDestroy() {
        helper.destroy()

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        helper.resume()
    }

    override fun onPause() {
        helper.pause()

        super.onPause()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply{
            putExtra(EXTRA_PROJECT_ID, currentTheme?.projectId ?: "")
            putExtra(EXTRA_THEME_ID, currentTheme?.id ?: "")
        })
        finish()
        overridePendingTransition(R.anim.anim_not_move, R.anim.anim_slide_out_bottom)
    }

    private fun requestSwipeTutorial(){
        val itemCount = helper.pager?.adapter?.itemCount ?: return
        if(itemCount > 1 && TutorialUtil.get().request(application, TutorialPreviewSwipe.NAME)){
            if(!tutorialPreviewDialog.isAdded)
                tutorialPreviewDialog.show(supportFragmentManager, "TutorialPreview")
        }
    }

    private fun showApplyDialog(){
        if(!applyDialog.isAdded)
            applyDialog.show(supportFragmentManager, "Apply")
    }

    private fun showRemoveWallpaperDialog(){
        if(!removeWallpaperDialog.isAdded)
            removeWallpaperDialog.show(supportFragmentManager, "RemoveWallpaper")
    }

    private fun gotoPreview(page:String, anim:Boolean = true){
        if(anim)
            scene.startTransition(
                binding.previewRoot,
                viewModel.isFadeAnim(page),
                onEnd={
                    viewModel.applyPage(page)
                })
        else{
            viewModel.applyPage(page)
        }
        when(page){
            PreviewPublicViewModel.PAGE_CONTENT,
            PreviewPublicViewModel.PAGE_DETAIL_CONTENT->{
                scene.applyToPreview(binding.previewRoot)
            }
            PreviewPublicViewModel.PAGE_HOME_PREVIEW->{
                scene.applyToPreviewHome(binding.previewRoot)
            }
            PreviewPublicViewModel.PAGE_LOCK_PREVIEW->{
                scene.applyToPreviewLock(binding.previewRoot)
            }
        }
    }

    inner class PreviewAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return PreviewHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is PreviewHolder && list[position] is FTheme)
                holder.bind(list[position] as FTheme)
        }

        fun getThemeByPos(pos:Int) : FTheme?{
            if(pos < 0 || pos >= list.size) return null
            return list[pos].let{
                if(it is FTheme) it
                else null
            }
        }
    }

    inner class PreviewHolder(parent:ViewGroup) : ExoPlayerViewHolder<FTheme>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_preview_public, parent, false)
    ) {
        private var hBinding: ViewPreviewPublicBinding? = null

        private val hViewModel: PreviewPublicHolderViewModel by lazy {
            ViewModelProvider(this@PreviewPublicActivity).get(
                toString(),
                PreviewPublicHolderViewModel::class.java
            )
        }

        private val ipObserver = Observer<Boolean> {
            if(!it){
                closePreview(false)
            }
        }

        init{
            view.findViewById<RecyclerView>(R.id.hash_tag).apply {
                adapter = TagAdapter()
                layoutManager = FlexboxLayoutManager(this@PreviewPublicActivity).apply {
                    flexWrap = FlexWrap.WRAP
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }
            }
        }

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.Main + job)

        private val hScene = PreviewPublicHolderScene()

        override fun bind(data: FTheme?) {
            hViewModel.bind(data)

            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply {
                lifecycleOwner = this@PreviewPublicActivity
                vm = hViewModel

                back.setOnClickListener {
                    onBackPressed()
                }

                link.setOnClickListener {
                    gotoLink(data)
                }

                closePreview.setOnClickListener {
                    closePreview()
                }

                nextPage.setOnClickListener {
                    nextPage()
                }

                expand.setOnClickListener {
                    toggleDetail()
                }

                expandDetail.setOnClickListener {
                    expandDetail()
                }

                apply.setOnClickListener {
                    selectTheme(data)
                }

                val adapter = hashTag.adapter as TagAdapter
                adapter.set(data?.themeHashTag)
            }

            helper.currentPos.observe(this@PreviewPublicActivity, observer)

            downloadTheme(data)
            bindPreload(data)
            bindUserAvatar(data?.user)
        }

        override fun resume() {
            bindTheme(hViewModel.theme)
            helper.idlePager.observe(this@PreviewPublicActivity, ipObserver)
        }

        override fun pause() {
            helper.idlePager.removeObserver(ipObserver)
            hBinding?.preload?.visibility = View.VISIBLE
        }

        private fun gotoLink(theme:FTheme?){
            theme?: return
            if(theme.themeLink == "") return

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(theme.themeLink))
            if(intent.resolveActivity(packageManager) != null) {
                hViewModel.logEnterThemeLink()
                startActivity(intent)
            }
        }

        private fun selectTheme(theme:FTheme?) {
            theme?: return
            if(isFinishing || isApplyWallpaper)
                return

            val isSetWallpaper = WallpaperUtil.isSetWallpaper(applicationContext)
            val isSelected = hViewModel.isSelectedTheme.value == true

            if (isSelected) {
                showRemoveWallpaperDialog()
                return
            }

            val requestCode = if(isSetWallpaper)
                if(isSelected) WallpaperUtil.REQUEST_SET_WALLPAPER
                else 0
            else
                if(isSelected) WallpaperUtil.REQUEST_SET_WALLPAPER
                else WallpaperUtil.REQUEST_SET_THEME

            hViewModel.selectPublicTheme()

            try {
                when {
                    requestCode != 0 -> {
                        isApplyWallpaper = true
                        WallpaperUtil.setWallpaper(this@PreviewPublicActivity, requestCode)
                    }
                    else -> {
                        isApplyWallpaper = false
                        appliedTheme()
                    }
                }
            }
            catch (e:ActivityNotFoundException){
                e.printStackTrace()
            }
        }

        private fun gotoDetail(page:String){
            when(page){
                PreviewPublicViewModel.PAGE_CONTENT->{
                    hScene.applyToContent(hBinding?.root, hBinding?.detailContainer)
                }
                PreviewPublicViewModel.PAGE_DETAIL_CONTENT->{
                    hScene.applyToContentDetail(hBinding?.root, hBinding?.detailContainer)
                }
                PreviewPublicViewModel.PAGE_HOME_PREVIEW,
                PreviewPublicViewModel.PAGE_LOCK_PREVIEW->{
                    hScene.applyToHideContent(hBinding?.root, hBinding?.detailContainer)
                }
            }
        }

        private fun expandDetail(){
            gotoPreview(PreviewPublicViewModel.PAGE_DETAIL_CONTENT)
            gotoDetail(PreviewPublicViewModel.PAGE_DETAIL_CONTENT)
        }

        private fun toggleDetail(){
            val togglePage = viewModel.togglePage ?: return

            gotoPreview(togglePage)
            gotoDetail(togglePage)
        }

        private fun closePreview(anim:Boolean=true){
            gotoPreview(PreviewPublicViewModel.PAGE_CONTENT, anim)
            gotoDetail(PreviewPublicViewModel.PAGE_CONTENT)
        }

        private fun nextPage(){
            val nextPage= viewModel.nextPreviewPage ?: return

            gotoPreview(nextPage)
            gotoDetail(nextPage)
        }

        private fun bindPreload(theme:FTheme?){
            theme?: return
            val img = hBinding?.preload ?: return
            img.setImageDrawable(null)

            scope.launch {
                if(theme.themeTitle == "개화"){
                    Log.d("TEST_LOG", "bindPreload : 개화")
                }

                val drawable = loadPreload(theme)
                if(theme.id != hViewModel.theme?.id){
                    bindPreload(hViewModel.theme)
                    return@launch
                }

                try {
                    Glide.with(img)
                        .load(drawable)
                        .into(img)
                }
                catch (e:IllegalStateException){
                    e.printStackTrace()
                }catch (e:IllegalArgumentException){
                    return@launch
                }
            }
        }

        private fun bindTheme(theme:FTheme?){
            theme?:return
            scope.launch{
                val source = loadTheme(theme)
                if (theme.id != hViewModel.theme?.id) {
                    bindTheme(hViewModel.theme)
                    return@launch
                }

                helper.bind(
                    hBinding?.player,
                    source
                ) {
                    hBinding?.preload?.visibility = View.INVISIBLE
                }
            }
        }

        private fun bindUserAvatar(user:User?){
            user?: return
            val img = hBinding?.userAvatar ?: return
            img.setImageDrawable(null)

            scope.launch{
                if(user.existAvatar){
                    val drawable = loadUserAvatar(user)
                    if(user.userUid != hViewModel.theme?.userUid){
                        bindUserAvatar(hViewModel.theme?.user)
                        return@launch
                    }

                    try {
                        Glide.with(img)
                            .load(drawable)
                            .apply(RequestOptions.circleCropTransform())
                            .into(img)
                    }
                    catch(e:IllegalStateException){
                        e.printStackTrace()
                    }catch (e:IllegalArgumentException){
                        return@launch
                    }
                }
                else{
                    img.setImageResource(R.drawable.default_user_avatar)
                }
            }
        }

        private suspend fun loadPreload(theme:FTheme?) = withContext(Dispatchers.IO + job){
            theme ?: return@withContext null
            if(!theme.existPreload)
                hViewModel.preloadDownload(theme)
            theme.getPreload(application)
        }

        private fun downloadTheme(theme:FTheme?) = lifecycleScope.launch(Dispatchers.IO + job) {
            theme ?: return@launch
            if (!theme.existTheme)
                hViewModel.themeDownload(theme)
        }

        private suspend fun loadTheme(theme:FTheme?) = withContext(Dispatchers.IO + job){
            theme ?: return@withContext null
            if(!theme.existTheme)
                hViewModel.themeDownload(theme)
            theme.getThemeSource(application)
        }

        private suspend fun loadUserAvatar(user: User?) = withContext(Dispatchers.IO + job){
            user ?: return@withContext null
            if(!user.updateAvatar)
                hViewModel.userAvatarDownload(user)
            user.getAvatar(application).image
        }
    }

    inner class TagAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return TagHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is TagHolder && list[position] is String)
                holder.bind(list[position] as String)
        }
    }

    inner class TagHolder(parent:ViewGroup) : BaseViewHolder<String>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_preview_tag, parent, false)
    ){
        private var binding:ViewPreviewTagBinding? = null

        override fun bind(data: String?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = this@PreviewPublicActivity

                tag.text = data?.let{ "#$it" } ?: ""
            }
        }
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun appliedTheme(){
        viewModel.logApplyFreeTheme()
        if(!DevelopUtil.isPromotionApplyAdBlock(this)) {
            BaseADUtil.get(application).requestAd {
                showApplyDialog()
            }
        }
    }

    companion object {
        const val EXTRA_PAGE = "Extra.Page"
        const val PAGE_ALL = "Page.All"
        const val PAGE_RECENT = "Page.Recent"
        const val PAGE_PROJECT = "Page.Project"

        const val EXTRA_PROMOTION_ID = "Extra.PromotionId"
        const val EXTRA_PROJECT_ID = "Extra.ProjectId"
        const val EXTRA_THEME_ID = "Extra.ThemeId"
    }
}