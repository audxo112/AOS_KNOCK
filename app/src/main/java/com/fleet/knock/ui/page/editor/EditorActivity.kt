package com.fleet.knock.ui.page.editor

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.*
import com.fleet.knock.info.editor.BaseThemeFrame
import com.fleet.knock.info.editor.ThemeFrame
import com.fleet.knock.ui.dialog.*
import com.fleet.knock.ui.page.gallery.GalleryActivity
import com.fleet.knock.ui.toast.SwapToast
import com.fleet.knock.utils.DevelopUtil
import com.fleet.knock.utils.FConfig
import com.fleet.knock.utils.WallpaperUtil
import com.fleet.knock.utils.admob.BaseADUtil
import com.fleet.knock.utils.encoding.LocalThemeEncodingJob
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SpaceHorizontalDivider
import com.fleet.knock.utils.tutorial.TutorialEditorBoomerang
import com.fleet.knock.utils.tutorial.TutorialEditorFrame
import com.fleet.knock.utils.tutorial.TutorialEditorTrim
import com.fleet.knock.utils.tutorial.TutorialUtil
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class EditorActivity : AppCompatActivity(){
    private lateinit var binding : ActivityEditorBinding
    private val viewModel by lazy{
        EditorViewModel.new(this, screenSize, uri, type, duration)
    }

    private val frameVM by lazy{
        EditorFrameViewModel.new(this, screenSize)
    }

    private val bgColorVM:EditorBgColorViewModel by viewModels()

    private val revertEditorDialog by lazy{
        RevertEditorDialog().apply{
            setGotoEdit {
                gotoReedit()
            }
            setGotoMain {
                gotoMain()
            }
        }
    }

    private val progressDialog by lazy{
        ProgressDialog().apply{
            setOnCancelListener {
                SwapToast.makeText(applicationContext, R.string.activity_editor_canceled_encoding).show()
                viewModel.encodingCancel()
            }
        }
    }

    private val tutorialEditorTrimDialog by lazy{
        TutorialEditorTrimDialog()
    }

    private val tutorialEditorBoomerangDialog by lazy{
        TutorialEditorBoomerangDialog()
    }

    private val tutorialEditorFrameDialog by lazy{
        TutorialEditorFrameDialog()
    }

    private val screenSize
        get() = Point().also {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                display?.getRealSize(it)
            else
                windowManager.defaultDisplay.getRealSize(it)
        }

    private val scene by lazy{
        EditorActivityScene(application)
    }

    private var isApplyWallpaper = false

    private val uri
//        get() = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//            Uri.parse(intent.extras?.getString(EXTRA_URI) ?: "")
//            else Uri.fromFile(File(intent.extras?.getString(EXTRA_URI) ?: ""))
        get() = Uri.fromFile(File(intent.extras?.getString(EXTRA_URI) ?: ""))

    private val type
        get() = intent.extras?.getString(EXTRA_TYPE) ?: ""

    private val duration
        get() = intent.extras?.getLong(EXTRA_DURATION) ?: 0L

    private val resizeMode
        get() = intent.extras?.getInt(EXTRA_DEFAULT_RESIZE_MODE)
            ?: AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT

    private val vibe by lazy{
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val frameAdapter by lazy{
        FrameAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BaseADUtil.get(application).loadAd()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor)
        binding.apply{
            lifecycleOwner = this@EditorActivity
            vm = viewModel
            frameVM = this@EditorActivity.frameVM

            back.setOnClickListener {
                onBackPressed()
            }

            save.setOnClickListener {
                encoding()
            }

            trimHelp.setOnClickListener {
                showTutorialEditorTrim()
            }

            playBtn.setOnClickListener {
                togglePlay()
            }

            toolFrame.setOnClickListener {
                gotoTool(EditorViewModel.TOOL_FRAME)
            }

            toolFrameBase.setOnClickListener {
                gotoFrame(EditorFrameViewModel.FRAME_TYPE_BASE)
            }

            toolFrameTemplate.setOnClickListener {
                gotoFrame(EditorFrameViewModel.FRAME_TYPE_TEMPLATE)
            }

            toolTrim.setOnClickListener {
                gotoTool(EditorViewModel.TOOL_TRIM)
            }

            toolBgColor.setOnClickListener {
                gotoTool(EditorViewModel.TOOL_BG_COLOR)
            }

            toolBoomerang.setOnClickListener {
                toggleBoomerang()
            }

            warningConfirm.setOnClickListener {
                gotoEditPage()
            }

            saveOnDevice.setOnClickListener {
                saveTheme()
            }

            apply.setOnClickListener {
                selectTheme()
            }

            homeScreen.setOnClickListener {
                gotoPreviewPage(EditorViewModel.PAGE_COMPLETE_HOME_PREVIEW)
            }

            lockScreen.setOnClickListener {
                gotoPreviewPage(EditorViewModel.PAGE_COMPLETE_LOCK_PREVIEW)
            }

            preview.setOnClickListener {
                gotoPreviewPage(viewModel.previewPage())
            }
        }

        val xPadding = screenSize.x / 2 - dpToPixel(20)
        binding.frameList.apply {
            addItemDecoration(
                SpaceHorizontalDivider(
                    xPadding,
                    xPadding,
                    dpToPixel(21)
                )
            )
            adapter = frameAdapter
            FrameSnapHelper().apply{
                setOnSnapPositionChange {
                    frameVM.selectedFrameIndex.value = it
                }
            }.attachToRecyclerView(this)
        }

        binding.bgColorList.apply {
            addItemDecoration(SpaceHorizontalDivider(dpToPixel(7), dpToPixel(7), dpToPixel(2)))
            adapter = BgColorAdapter().apply {
                set(bgColorVM.bgColorList)
            }
            val lastSelectedIndex = bgColorVM.lastSelectedBgColorIndex
            if(lastSelectedIndex != -1)
                smoothScrollToPosition(lastSelectedIndex)
        }

        binding.player.apply{
            progressBar = binding.rangeProgress
            seekBar = binding.seekBar
            player = viewModel.simplePlayer
            resizeMode = this@EditorActivity.resizeMode
        }

        lifecycleScope.launch {
            frameAdapter.addAll(frameVM.baseFrameList)
            frameAdapter.addAll(frameVM.getTemplateFrameList())
        }

        loadEditVideo()

        if(viewModel.isTooLong)
            gotoWarningTooLongPage(false)
        else
            gotoEditPage(false)
    }

    override fun onResume() {
        super.onResume()

        playWhenReady(true)
    }

    override fun onPause() {
        playWhenReady(false)

        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == WallpaperUtil.REQUEST_SET_THEME){
            isApplyWallpaper = false
            if(WallpaperUtil.isSetWallpaper(this)){
                appliedTheme()
            }
            else{
                viewModel.restoreSelectTheme()
            }
        }
    }

    override fun onBackPressed() {
        when(viewModel.backAction()){
            EditorViewModel.ACTION_GOTO_GALLERY
                    -> gotoGallery(Activity.RESULT_CANCELED)
            EditorViewModel.ACTION_SHOW_REVERT_EDITOR_DIALOG
                    -> showRevertEditorDialog()
            EditorViewModel.ACTION_GOTO_COMPLETE
                    -> gotoPreviewPage(EditorViewModel.PAGE_COMPLETE)
        }
    }

    private fun loadEditVideo(){
        lifecycleScope.launch{
            val uri = viewModel.makeEditVideoUri()
            if(uri == null){
                Toast.makeText(applicationContext, R.string.activity_editor_load_failed, Toast.LENGTH_SHORT).show()
                gotoGallery(Activity.RESULT_CANCELED)
            }
            else{
                binding.player.setVideo(uri)
            }
        }
    }

    private fun showTutorialEditorBoomerangDialog(){
        tutorialEditorBoomerangDialog.setOnApplyCallback{
            toggleBoomerang()
        }

        if(!tutorialEditorBoomerangDialog.isAdded)
            tutorialEditorBoomerangDialog.show(supportFragmentManager, "TutorialEditorBoomerang")
    }

    private fun showRevertEditorDialog(){
        if(!revertEditorDialog.isAdded)
            revertEditorDialog.show(supportFragmentManager, "RevertEditor")
    }

    private fun showTutorialEditorFrame(){
        if(!tutorialEditorFrameDialog.isAdded)
            tutorialEditorFrameDialog.show(supportFragmentManager, "TutorialEditorFrame")
    }

    private fun showTutorialEditorTrim(){
        if(!tutorialEditorTrimDialog.isAdded)
            tutorialEditorTrimDialog.show(supportFragmentManager, "TutorialEditorTrim")
    }

    private fun showProgressDialog(){
        if(!progressDialog.isAdded)
            progressDialog.show(supportFragmentManager, "ProgressDialog")
    }

    private fun hideProgressDialog(){
        try {
            if (progressDialog.isAdded)
                progressDialog.dismiss()
        }
        catch(e:IllegalStateException){
            val crash = FirebaseCrashlytics.getInstance()
            crash.log(e.message ?: "hideProgressDialog IllegalStateException")
        }
    }

    private fun toggleBoomerang(){
        if(TutorialUtil.get().request(application, TutorialEditorBoomerang.NAME)){
            showTutorialEditorBoomerangDialog()
        }
        else{
            val boomerang = viewModel.boomerang.value == false
            viewModel.boomerang.value = boomerang
            if(boomerang)
                SwapToast.makeText(application, R.string.activity_editor_apply_boomerang).show()
            else
                SwapToast.cancel()
        }
    }

    private fun playWhenReady(play:Boolean){
        viewModel.simplePlayer.playWhenReady = play
        binding.player.mode = viewModel.mode()
    }


    private fun togglePlay(){
        playWhenReady(!viewModel.simplePlayer.playWhenReady)
    }

    private fun gotoWarningTooLongPage(anim:Boolean = true){
        scene.gotoWarningTooLong(binding.motionLayout, anim, onEnd = {
            viewModel.applyPage(EditorViewModel.PAGE_WARNING_TOO_LONG)
            binding.player.mode = viewModel.mode()
        })
    }

    private fun gotoReedit(){
        viewModel.deleteTheme()
        gotoEditPage()
    }

    private fun gotoEditPage(anim:Boolean = true){
        if(TutorialUtil.get().request(application, TutorialEditorFrame.NAME))
            showTutorialEditorFrame()

        val change = viewModel.videoChangePage(EditorViewModel.PAGE_EDITOR)

        scene.gotoEditor(binding.motionLayout, anim,
            onStart = {
                if(change){
                    binding.player.startRestore()
                    binding.player.setVideo(viewModel.getEditVideoUri())
                }
            },
            onEnd = {
                if(change) {
                    binding.player.endRestore()
                }
                viewModel.applyPage(EditorViewModel.PAGE_EDITOR)
                binding.player.mode = viewModel.mode()
            })
    }

    private fun gotoPreviewPage(page:String){
        if(viewModel.isSamePage(page))
            return

        when(page){
            EditorViewModel.PAGE_COMPLETE -> scene.gotoComplete(binding.motionLayout)
            EditorViewModel.PAGE_COMPLETE_HOME_PREVIEW -> scene.gotoCompleteHomePreview(binding.motionLayout)
            EditorViewModel.PAGE_COMPLETE_LOCK_PREVIEW -> scene.gotoCompleteLockPreview(binding.motionLayout)
            else -> scene.gotoComplete(binding.motionLayout)
        }
        viewModel.applyPage(page)
    }

    private fun gotoCompletePage(){
        val change = viewModel.videoChangePage(EditorViewModel.PAGE_COMPLETE)
        scene.gotoComplete(binding.motionLayout,
            onStart = {
                if(change){
                    binding.player.startClear()
                    binding.player.setVideo(viewModel.getThemeLocal().getThemeProvider(applicationContext).uri)
                }
            },
            onEnd = {
                if(change){
                    binding.player.endClear()
                }
                viewModel.applyPage(EditorViewModel.PAGE_COMPLETE)
                binding.player.mode = viewModel.mode()
            })
    }

    private fun gotoMain(){
        if(!isFinishing) {
            setResult(GalleryActivity.RESULT_FINISH)
            finish()
        }
    }

    private fun gotoGallery(result:Int){
        if(!isFinishing) {
            setResult(result)
            finish()
        }
    }

    private fun gotoTool(tool:String){
        if(viewModel.isSameTool(tool))
            return

        if(tool == EditorViewModel.TOOL_TRIM &&
            TutorialUtil.get().request(application, TutorialEditorTrim.NAME)){
            showTutorialEditorTrim()
        }

        viewModel.applyTool(tool)
        binding.player.mode = viewModel.mode()
        scene.toolIndicator(binding.toolNavContainer, viewModel.toolTargetId(tool))
    }

    private fun gotoFrame(frameType:String){
        if(frameType == EditorFrameViewModel.FRAME_TYPE_BASE)
            selectFrame(frameVM.baseFrameIndex)
        else selectFrame(frameVM.templateFrameIndex)

//        if(frameVM.isSameFrameType(frameType))
//            return
//
//        frameVM.applyFrameType(frameType)

//        if(frameType == EditorFrameViewModel.FRAME_TYPE_TEMPLATE &&
//                viewModel.needTutorialFrameTemplate){
//            viewModel.needTutorialFrameTemplate = false
//        }

//        binding.frameList.adapter = when(frameType){
//            EditorFrameViewModel.FRAME_TYPE_BASE -> baseFrameAdapter
//            EditorFrameViewModel.FRAME_TYPE_TEMPLATE -> templateFrameMiniAdapter
//            else -> baseFrameAdapter
//        }
    }

    private fun encoding(){
        lifecycleScope.launch{
            showProgressDialog()

            val boomerang = viewModel.boomerang.value == true

            val result = viewModel.encodingTheme(
                binding.player.makeFilterComplex(screenSize, boomerang)
            ) { frameNumber ->
                progressDialog.setProgress(
                    binding.player.calculateProgress(frameNumber, boomerang)
                )
            }

            hideProgressDialog()

            when(result){
                LocalThemeEncodingJob.RESULT_OK -> {
                    FConfig.get(application).isDoneEncode = true
                    viewModel.logEncoding(binding.player.encodingLog())
                    frameVM.updateRecentUsedTime(binding.player.templateFrame)
                    bgColorVM.saveLastSelectedBgColor()
                    gotoCompletePage()
                }
                LocalThemeEncodingJob.RESULT_CANCEL ->
                    SwapToast.makeText(applicationContext, R.string.activity_editor_canceled_encoding).show()
                else ->
                    SwapToast.makeText(applicationContext, R.string.activity_editor_failed_encoding).show()
            }
        }

//        val command = StringBuilder()
//
//        command.append("ffmpeg -i src.mp4 ")
//        command.append(binding.player.makeFilterComplex(screenSize, viewModel.boomerang.value == true))
//        command.append("-vcodec libx264 ")
//        command.append("-crf 28 ")
//        command.append("-an ")
//        command.append("-y ")
//        command.append("dst.mp4")
//
//        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        val data = ClipData.newPlainText("FFmpeg", command.toString())
//        clipboard.setPrimaryClip(data)
//
//        Log.d("TEST_ENCODING", command.toString())
    }

    private fun saveTheme(){
        if(viewModel.existSavedFile.value == true) {
            return
        }

        lifecycleScope.launch{
            val result = viewModel.saveTheme()
            if(result) {
                SwapToast.makeText(applicationContext, R.string.activity_editor_complete_save_on_device_success).show()
            }
            else
                SwapToast.makeText(applicationContext, R.string.activity_editor_complete_save_on_device_failed).show()
        }
    }

    private fun selectTheme(){
        if(isApplyWallpaper)
            return
        isApplyWallpaper = true

        val isSetWallpaper = WallpaperUtil.isSetWallpaper(applicationContext)

        val requestCode = 
            if(isSetWallpaper) 0 
            else WallpaperUtil.REQUEST_SET_THEME

        viewModel.selectLocalTheme()

        try {
            when {
                requestCode != 0 -> {
                    WallpaperUtil.setWallpaper(this, requestCode)
                }
                else -> {
                    isApplyWallpaper = false
                    appliedTheme()
                }
            }
        }
        catch (e: ActivityNotFoundException){
            e.printStackTrace()
        }
    }

    private fun selectFrame(pos:Int){
        val offset = screenSize.x / 2 - dpToPixel(30.5f)
        val layoutManager = binding.frameList.layoutManager as LinearLayoutManager? ?: return

        layoutManager.scrollToPositionWithOffset(pos, offset)
        frameVM.selectedFrameIndex.value = pos
    }

    inner class FrameAdapter : BaseAdapter(){
        private val TYPE_BASE = 0
        private val TYPE_TEMPLATE = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return if(viewType == TYPE_BASE) BaseFrameHolder(parent)
            else TemplateFrameMiniHolder(parent)
        }

        override fun getItemViewType(position: Int): Int {
            return when (list[position]) {
                is BaseThemeFrame -> TYPE_BASE
                is ThemeFrame -> TYPE_TEMPLATE
                else -> TYPE_BASE
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is BaseFrameHolder && list[position] is BaseThemeFrame)
                holder.bind(list[position] as BaseThemeFrame)
            else if(holder is TemplateFrameMiniHolder && list[position] is ThemeFrame)
                holder.bind(list[position] as ThemeFrame)
        }
    }

    inner class BaseFrameHolder(parent:ViewGroup) : BaseViewHolder<BaseThemeFrame>(
        LayoutInflater.from(parent.context).inflate(R.layout.view_editor_frame_mini_base, parent, false)
    ){
        private var hBinding: ViewEditorFrameMiniBaseBinding? = null

        init{
            frameVM.selectedFrameIndex.observe(this@EditorActivity, Observer {
                selectedIndex(it)
            })
        }

        override fun bind(data: BaseThemeFrame?) {
            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply{
                lifecycleOwner = this@EditorActivity
                base = data

                baseFrame.setImageResource(data?.unselectedResId ?: R.drawable.ic_editor_tool_frame_0_unselected)
                baseFrame.setOnClickListener {
                    selectFrame(adapterPosition)
                }

                selectedIndex(frameVM.selectedFrameIndex.value)
            }
        }

        private fun selectedIndex(index:Int?){
            index ?: return
            val base = hBinding?.base
            if(index == adapterPosition){
                if(base != null) {
                    binding.player.setFrameBase(base)
                    vibe.vibrate(1)
                }

                hBinding?.baseFrame?.setImageResource(
                    hBinding?.base?.selectedResId ?: R.drawable.ic_editor_tool_frame_0_selected
                )
            }
            else{
                hBinding?.baseFrame?.setImageResource(
                    hBinding?.base?.unselectedResId ?: R.drawable.ic_editor_tool_frame_0_unselected
                )
            }
        }
    }

    inner class TemplateFrameMiniHolder(parent:ViewGroup) : BaseViewHolder<ThemeFrame>(
        LayoutInflater.from(parent.context).inflate(R.layout.view_editor_frame_mini_template, parent, false)
    ){
        private var hBinding:ViewEditorFrameMiniTemplateBinding? = null

        private val hViewModel : EditorFrameTemplateHolderViewModel by lazy{
            ViewModelProvider(this@EditorActivity).get(
                toString(),
                EditorFrameTemplateHolderViewModel::class.java
            )
        }

        private val job = Job()

        init{
            frameVM.selectedFrameIndex.observe(this@EditorActivity, Observer {
                selectedIndex(it)
            })
        }

        override fun bind(data: ThemeFrame?) {
            hViewModel.bind(data)

            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply{
                lifecycleOwner = this@EditorActivity
                vm = hViewModel

                container.setOnClickListener {
                    selectFrame(adapterPosition)
                }
            }

            selectedIndex(frameVM.selectedFrameIndex.value)

            downloadTemplate(data)
            bindThumbnail(data)
        }

        private fun selectedIndex(index:Int?){
            index ?: return
            val template = hViewModel.template
            if(index == adapterPosition){
                if(template != null) {
                    hViewModel.updateConfirmTime()
                    binding.player.setFrameTemplate(template)
                    vibe.vibrate(1)
                }
                hBinding?.selected?.setBackgroundResource(R.drawable.bg_white_r2_box)
            }
            else{
                hBinding?.selected?.setBackgroundResource(R.drawable.bg_gray2_r2_i1_box)
            }
        }

        private fun bindThumbnail(frame:ThemeFrame?){
            frame?: return
            val img = hBinding?.templateFrame ?: return
            img.setImageDrawable(null)

            lifecycleScope.launch{
                val template = hViewModel.template
                val drawable = loadMiniThumbnail(frame)
                if(frame.id != template?.id) {
                    bindThumbnail(template)
                    return@launch
                }

                Glide.with(img)
                    .load(drawable)
                    .into(img)
            }
        }

        private fun downloadTemplate(frame:ThemeFrame?){
            frame?: return
            if(frame.frameUpdate)
                return

            lifecycleScope.launch{
                downloadFrame(frame)

                if(frame.frameUpdate && frameVM.selectedFrameIndex.value == adapterPosition){
                    binding.player.setFrameTemplate(frame)
                }
            }
        }

        private suspend fun loadMiniThumbnail(frame:ThemeFrame?) = withContext(Dispatchers.IO + job){
            frame ?: return@withContext null
            if(!frame.miniThumbnailUpdate)
                frameVM.miniThumbnailDownload(frame)
            frame.getMiniThumbnail(application).image
        }

        private suspend fun downloadFrame(frame:ThemeFrame?) = withContext(Dispatchers.IO + job){
            frame ?: return@withContext
            if(!frame.frameUpdate)
                frameVM.frameDownload(frame)
        }
    }

    inner class BgColorAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return BgColorHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is BgColorHolder){
                holder.bind((list[position] as Long).toInt())
            }
        }
    }

    inner class BgColorHolder(parent:ViewGroup) : BaseViewHolder<Int>(
        LayoutInflater.from(parent.context).inflate(R.layout.view_editor_bg_color, parent, false)
    ){
        private var hBinding:ViewEditorBgColorBinding? = null

        private val bgColor = GradientDrawable().apply{
            shape = GradientDrawable.OVAL
        }

        init{
            bgColorVM.selectedBgColorIndex.observe(this@EditorActivity, Observer {
                selectedIndex(it)
            })
        }

        override fun bind(data: Int?) {
            hBinding = DataBindingUtil.bind(view)
            hBinding?.apply{
                lifecycleOwner = this@EditorActivity

                container.setOnClickListener {
                    selectColor(data)
                }

                bgColor.setColor(data ?: Color.BLACK)
                color.background = bgColor

                selectedIndex(bgColorVM.selectedBgColorIndex.value)
            }
        }

        private fun selectedIndex(index:Int?){
            index ?: return
            if(index == adapterPosition){
                hBinding?.selected?.setBackgroundResource(R.drawable.bg_white_circle)
            }
            else{
                hBinding?.selected?.setBackgroundResource(R.drawable.bg_gray2_i1_circle)
            }
        }

        private fun selectColor(color : Int?){
            color ?: return

            bgColorVM.selectedBgColorIndex.value = adapterPosition
            bgColorVM.lastSelectedBgColor = color.toLong()
            binding.player.setShutterBackgroundColor(color)
        }
    }

    fun dpToPixel(dp:Float) = resources.displayMetrics.let{
        (dp * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun dpToPixel(dp:Int) = resources.displayMetrics.let{
        (dp.toFloat() * (it.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun appliedTheme(){
        viewModel.logApplyLocalTheme()

        if(!DevelopUtil.isEncodingApplyAdBlock(this)) {
            BaseADUtil.get(application).requestAd {
                gotoGallery(Activity.RESULT_OK)
            }
        }
    }

    companion object{
        const val EXTRA_TYPE = "Extra.Type"
        const val TYPE_GIF = "Type.Gif"
        const val TYPE_VIDEO = "Type.Video"

        const val EXTRA_DURATION = "Extra.Duration"
        const val EXTRA_DEFAULT_RESIZE_MODE = "Extra.DefaultResizeMode"
        const val EXTRA_URI = "Extra.Uri"
    }
}