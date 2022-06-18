package com.fleet.knock.ui.page.preview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.ActivityPreviewBinding
import com.fleet.knock.databinding.ViewPreviewLocalBinding
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.ui.dialog.ApplyDialog
import com.fleet.knock.ui.dialog.DeleteThemeDialog
import com.fleet.knock.ui.dialog.RemoveWallpaperDialog
import com.fleet.knock.ui.dialog.TutorialPreviewSwipeDialog
import com.fleet.knock.ui.toast.SwapToast
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.GAUtil
import com.fleet.knock.utils.StorageUtil
import com.fleet.knock.utils.admob.BaseADUtil
import com.fleet.knock.utils.WallpaperUtil
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.ExoPlayerPagerHelper
import com.fleet.knock.utils.recycler.ExoPlayerViewHolder
import com.fleet.knock.utils.tutorial.TutorialPreviewSwipe
import com.fleet.knock.utils.tutorial.TutorialUtil
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

class PreviewLocalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    private val viewModel:PreviewLocalViewModel by viewModels()

    private val helper by lazy{
        ExoPlayerPagerHelper(this)
    }

    private val applyDialog by lazy{
        ApplyDialog()
    }

    private val removeWallpaperDialog by lazy{
        RemoveWallpaperDialog().apply{
            setOnRemoveWallpaper {
                WallpaperUtil.removeWallpaper(this@PreviewLocalActivity,
                    onProgress = {
                        viewModel.isLoading.value = it
                    },
                    onComplete = {
                        viewModel.removeSelectTheme()
                    })
            }
        }
    }

    private val deleteThemeDialog by lazy{
        DeleteThemeDialog()
    }

    private val tutorialPreviewDialog by lazy{
        TutorialPreviewSwipeDialog()
    }

    private val selectedThemeId
        get() = intent.extras?.getLong(EXTRA_THEME_ID) ?: -1L

    private var isApplyWallpaper = false

    private val scene = PreviewScene()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BaseADUtil.get(application).loadAd()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview)
        binding.apply{
            lifecycleOwner = this@PreviewLocalActivity
            vm = viewModel

            homeScreen.setOnClickListener {
                gotoPreview(PreviewPublicViewModel.PAGE_HOME_PREVIEW)
            }

            lockScreen.setOnClickListener {
                gotoPreview(PreviewPublicViewModel.PAGE_LOCK_PREVIEW)
            }
        }

        helper.apply {
            pager = binding.pager
            setAdapter(PreviewAdapter())
        }

        lifecycleScope.launch{
            val themeList = viewModel.getThemeList()

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

    private fun showDeleteThemeDialog(deleteCallback:()->Unit){
        if(!deleteThemeDialog.isAdded){
            deleteThemeDialog.setOnThemeDeleteListener(deleteCallback)
            deleteThemeDialog.show(supportFragmentManager, "DeleteTheme")
        }
    }

    private fun gotoPreview(page:String, anim:Boolean = true){
        if(anim){
            scene.startTransition(
                binding.previewRoot,
                viewModel.isFadeAnim(page),
                onEnd = {
                    viewModel.applyPage(page)
                })
        }
        else{
            viewModel.applyPage(page)
        }

        when(page){
            PreviewLocalViewModel.PAGE_CONTENT->{
                scene.applyToPreview(binding.previewRoot)
            }
            PreviewLocalViewModel.PAGE_HOME_PREVIEW->{
                scene.applyToPreviewHome(binding.previewRoot)
            }
            PreviewLocalViewModel.PAGE_LOCK_PREVIEW->{
                scene.applyToPreviewLock(binding.previewRoot)
            }
        }
    }

    inner class PreviewAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return PreviewHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is PreviewHolder && list[position] is FThemeLocal)
                holder.bind(list[position] as FThemeLocal)
        }
    }

    inner class PreviewHolder(parent:ViewGroup) : ExoPlayerViewHolder<FThemeLocal>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_preview_local, parent, false)
    ) {
        private var hBinding: ViewPreviewLocalBinding? = null

        private val hViewModel: PreviewLocalHolderViewModel by lazy {
            ViewModelProvider(this@PreviewLocalActivity).get(
                toString(),
                PreviewLocalHolderViewModel::class.java
            )
        }

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.Main + job)

        private val menu:PopupMenu by lazy{
            PopupMenu(this@PreviewLocalActivity, hBinding?.more, Gravity.END).apply{
                menuInflater.inflate(R.menu.menu_local_preview, menu)

                setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.delete -> delete()
                    }
                    true
                }
            }
        }
        private val ipObserver = Observer<Boolean> {
            if(!it){
                closePreview(false)
            }
        }

        private val hScene = PreviewLocalHolderScene()

        override fun bind(data: FThemeLocal?) {
            hViewModel.bind(data)

            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply{
                lifecycleOwner = this@PreviewLocalActivity
                vm = hViewModel

                back.setOnClickListener {
                    onBackPressed()
                }

                more.setOnClickListener {
                    menu.show()
                }

                closePreview.setOnClickListener {
                    closePreview()
                }

                inset.setOnClickListener{
                    nextPage()
                }

                saveOnDevice.setOnClickListener {
                    save()
                }

                apply.setOnClickListener {
                    selectTheme()
                }
            }

            helper.currentPos.observe(this@PreviewLocalActivity, observer)

            bindPreload(data)
        }

        override fun resume() {
            bindTheme(hViewModel.theme)
            helper.idlePager.observe(this@PreviewLocalActivity, ipObserver)
        }

        override fun pause() {
            helper.idlePager.removeObserver(ipObserver)
            hBinding?.preload?.visibility = View.VISIBLE
        }

        private fun selectTheme() {
            if(isFinishing || isApplyWallpaper)
                return

            val isSetWallpaper = WallpaperUtil.isSetWallpaper(application)
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

            hViewModel.selectLocalTheme()

            try {
                when {
                    requestCode != 0 -> {
                        isApplyWallpaper = true
                        WallpaperUtil.setWallpaper(this@PreviewLocalActivity, requestCode)
                    }
                    else -> {
                        appliedTheme()
                    }
                }
            }
            catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }

        private fun edit(){

        }

        private fun delete(){
            showDeleteThemeDialog {
                hViewModel.deleteTheme()
                helper.delete(adapterPosition)

                if(helper.isEmpty()){
                    finish()
                }
            }
        }

        private fun save(){
            if(hViewModel.existSavedFile.value == true) {
                return
            }

            lifecycleScope.launch{
                val result = hViewModel.saveTheme()
                if(result) {
                    SwapToast.makeText(applicationContext, R.string.view_preview_local_save_on_device_success).show()
                }
                else
                    SwapToast.makeText(applicationContext, R.string.view_preview_local_save_on_device_failed).show()
            }
        }

        private fun closePreview(anim:Boolean=true){
            gotoPreview(PreviewLocalViewModel.PAGE_CONTENT, anim)
            gotoDetail(PreviewLocalViewModel.PAGE_CONTENT)
        }

        private fun nextPage(){
            val nextPage = viewModel.nextPage ?: return

            gotoPreview(nextPage)
            gotoDetail(nextPage)
        }

        private fun gotoDetail(page:String){
            when(page){
                PreviewLocalViewModel.PAGE_CONTENT->{
                    hScene.applyToContent(hBinding?.root)
                }
                PreviewLocalViewModel.PAGE_LOCK_PREVIEW,
                PreviewLocalViewModel.PAGE_HOME_PREVIEW->{
                    hScene.applyToHideContent(hBinding?.root)
                }
            }
        }

        private fun bindPreload(theme:FThemeLocal?){
            theme?: return
            val img = hBinding?.preload ?: return
            img.setImageDrawable(null)

            scope.launch {
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

        private fun bindTheme(theme:FThemeLocal?){
            theme?:return
            scope.launch {
                val source = loadTheme(theme)
                if(theme.id != hViewModel.theme?.id){
                    bindTheme(hViewModel.theme)
                    return@launch
                }

                helper.bind(
                    hBinding?.player,
                    source
                ) {
                    hBinding?.preload?.visibility =
                        if(theme.id == hViewModel.theme?.id && play) View.INVISIBLE
                        else View.VISIBLE
                }
            }
        }

        private suspend fun loadPreload(theme:FThemeLocal?) = withContext(Dispatchers.IO + job){
            theme?: return@withContext null
            theme.getPreload(application)
        }

        private suspend fun loadTheme(theme:FThemeLocal?) = withContext(Dispatchers.IO + job){
            theme?: return@withContext null
            theme.getThemeSource(application)
        }
    }

    private fun appliedTheme(){
        viewModel.logApplyLocalTheme()
        if(!DevelopUtil.isLocalApplyAdBlock(this)) {
            BaseADUtil.get(application).requestAd {
                showApplyDialog()
            }
        }
    }

    companion object {
        const val EXTRA_THEME_ID = "Extra.ThemeId"
    }
}