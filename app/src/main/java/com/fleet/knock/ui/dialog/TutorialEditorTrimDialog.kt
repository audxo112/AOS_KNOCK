package com.fleet.knock.ui.dialog

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fleet.knock.R
import com.fleet.knock.databinding.DialogTutorialEditorTrimBinding
import com.fleet.knock.databinding.ViewTutorialEditorTextBinding
import com.fleet.knock.ui.view.RangeProgressBar
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import kotlin.math.abs

class TutorialEditorTrimDialog : CompatDialog(){

    private lateinit var binding: DialogTutorialEditorTrimBinding

    private val currentPosition = MutableLiveData<Int>()

    private val videoPanelIntroAnim by lazy{
        ValueAnimator.ofFloat(0f, 1f).apply{
            duration = 300
            repeatCount = 0

            addUpdateListener {
                binding.videoPanelControl.alpha = it.animatedValue as Float
            }
        }
    }

    private val videoPanelInitAnim by lazy{
        ValueAnimator.ofInt().apply{
            repeatCount = 0
            interpolator = LinearInterpolator()

            addUpdateListener {
                binding.rangeProgress.setProgress((it.animatedValue as Int).toLong(), true)
            }
        }
    }

    private val videoPanelIdleAnim by lazy{
        ValueAnimator.ofInt(40, 190, 40).apply{
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener {
                binding.rangeProgress.setProgress((it.animatedValue as Int).toLong(), true)
            }
        }
    }

    private val progressIndicatorIntroAnim by lazy{
        ValueAnimator.ofFloat(0f, 1f).apply{
            duration = 300
            repeatCount = 0

            addUpdateListener {
                binding.progressIndicator.alpha = it.animatedValue as Float
            }
        }
    }

    private val progressIndicatorIdleAnim by lazy{
        ValueAnimator.ofFloat(0f, dpToPixel(22), 0f).apply{
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateInterpolator()

            addUpdateListener {
                binding.progressIndicator.translationY = it.animatedValue as Float
            }
        }
    }

    private val tapThumbRippleAnim by lazy{
        ValueAnimator.ofFloat(0f, 1.5f).apply{
            duration = 800
            repeatCount = 1
            interpolator = DecelerateInterpolator()

            addUpdateListener {
                val v = it.animatedValue as Float
                val a = if(v > 1f) 1 - (v - 1f) / 0.5f else 1f
                binding.tapThumbRippleEffect.apply{
                    scaleX = v
                    scaleY = v
                    alpha = a
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.apply{
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.BottomUpDialogStyle
            setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_tutorial_editor_trim, container, false)
        binding.apply{
            dialog = this@TutorialEditorTrimDialog
            lifecycleOwner = viewLifecycleOwner

            close.setOnClickListener {
                dismiss()
            }

            root.attachTargetView(textPager)

            textPager.apply{
                adapter = TutorialTextAdapter()
                setOnPageSelectedListener(onSelectedPage)
            }

            indicator.attachPagerRecyclerView(textPager)

            rangeProgress.apply{
                max = 230
                interactiveEvent = false

                setOnChangedListener(object: RangeProgressBar.OnChangedListener{
                    override fun onProgressChanged(progress: Long, posX: Float) {
                        videoPanelControl.x = posX + rangeProgress.x - videoPanelControl.width / 2
                        progressIndicator.x = posX + rangeProgress.x - progressIndicator.width / 2
                    }
                })
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onStartTutorial()
    }

    override fun onResume() {
        super.onResume()

        initDialog()
        initData()
    }

    override fun onDismiss(dialog: DialogInterface) {
        cancelAnims(
            videoPanelIntroAnim,
            videoPanelInitAnim,
            videoPanelIdleAnim,
            progressIndicatorIntroAnim,
            progressIndicatorIdleAnim,
            tapThumbRippleAnim
        )

        currentPosition.value = 0

        super.onDismiss(dialog)
    }

    private fun initDialog(){
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dpToPixel(428).toInt() + getNavHeight())
    }

    private fun initData(){
        (binding.textPager.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        currentPosition.value = 0
    }

    private fun cancelAnims(vararg anims:ValueAnimator){
        for (anim in anims){
            anim.apply{
                startDelay = 0L
                removeAllListeners()
                if(isRunning) cancel()
            }
        }
    }

    private fun onStartTutorial(){
        binding.rangeProgress.apply {
            resetThumb()

            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setProgress(40, true)

                    cancelAnims(videoPanelIdleAnim)
                    videoPanelIdleAnim.apply {
                        setIntValues(40, 190, 40)
                    }.start()
                }
            })
        }
    }

    private val onSelectedPage = onSelectedPage@{ current:Int, before:Int ->
        currentPosition.value = current
        if(before == 0){
            startVideoPanelNextAnim()
        }
        else if(before == 1 && current == 0){
            startProgressIndicatorBeforeAnim()
        }
        else if(before == 1 && current == 2){
            startProgressIndicatorNextAnim()
        }
        else if(before == 2){
            startTapThumbBeforeAnim()
        }

        return@onSelectedPage
    }

    private fun startVideoPanelNextAnim(){
        cancelAnims(
            videoPanelIdleAnim,
            videoPanelInitAnim
        )

        var job1 ={}
        var job2 ={}
        var job3 ={}

        job1 = {
            cancelAnims(videoPanelInitAnim)

            val progress = binding.rangeProgress.progress.toInt()
            videoPanelInitAnim.apply{
                setIntValues(progress, 190)
                duration = 1000L / 300L * abs(progress - 190)

                doOnEnd { job2() }
            }.start()
        }

        job2 = {
            cancelAnims(progressIndicatorIntroAnim)

            progressIndicatorIntroAnim.apply{
                doOnStart {
                    binding.progressIndicator.translationY = 0f
                    binding.videoPanelControl.alpha = 0f
                }
                doOnEnd { job3() }
            }.start()
        }

        job3 = {
            cancelAnims(progressIndicatorIdleAnim)

            progressIndicatorIdleAnim.apply{
                doOnEnd {
                    binding.progressIndicator.alpha = 0f
                }
            }.start()
        }

        job1()
    }


    private fun startProgressIndicatorBeforeAnim(){
        cancelAnims(
            videoPanelIntroAnim,
            videoPanelInitAnim,
            videoPanelIdleAnim,
            progressIndicatorIntroAnim,
            progressIndicatorIdleAnim,
            tapThumbRippleAnim
        )

        binding.apply{
            rangeProgress.resetThumb()
            tapThumbRippleContainer.alpha = 0f
        }

        var job1 = {}
        var job2 = {}

        job1 = {
            cancelAnims(videoPanelIntroAnim)

            videoPanelIntroAnim.apply{
                doOnStart {
                    binding.progressIndicator.alpha = 0f
                }
                doOnEnd {
                    job2()
                }
            }.start()
        }

        job2 = {
            cancelAnims(videoPanelIdleAnim)
            videoPanelIdleAnim.apply{
                setIntValues(190, 40, 190)
                doOnStart {
                }
            }.start()
        }

        job1()
    }

    private fun startProgressIndicatorNextAnim(){
        cancelAnims(
            videoPanelIntroAnim,
            videoPanelInitAnim,
            videoPanelIdleAnim,
            progressIndicatorIntroAnim,
            progressIndicatorIdleAnim,
            tapThumbRippleAnim
        )

        binding.apply{
            progressIndicator.alpha = 0f
        }

        var job2 = {}
        var job3 = {}
        var job4 = {}
        var job5 = {}

        val job1 = {
            val progress = binding.rangeProgress.progress.toInt()
            if(progress != 190){
                cancelAnims(videoPanelInitAnim)
                videoPanelInitAnim.apply{
                    setIntValues(progress, 190)
                    duration = 1000L / 300L * abs(progress - 190)
                    doOnEnd {
                        job2()
                    }
                }.start()
            }
            else{
                job2()
            }
        }

        job2 = {
            cancelAnims(tapThumbRippleAnim)
            tapThumbRippleAnim.apply{
                startDelay = 500L
                doOnStart {
                    binding.tapThumbRippleContainer.apply{
                        x = binding.rangeProgress.endScreenPos + binding.rangeProgress.x - width /2
                        alpha = 1f
                    }
                }
                doOnEnd {
                    binding.tapThumbRippleContainer.alpha = 0f
                    binding.rangeProgress.tapEndThumb()
                    job3()
                }
            }.start()
        }

        job3 = {
            cancelAnims(videoPanelInitAnim)
            videoPanelInitAnim.apply{
                startDelay = 500L
                setIntValues(190, 80)
                duration = 2000L / 300L * 110

                doOnStart {
                    binding.videoPanelControl.alpha = 1f
                }
                doOnEnd{
                    binding.videoPanelControl.alpha = 0f
                    job4()
                }
            }.start()
        }

        job4 = {
            cancelAnims(tapThumbRippleAnim)
            tapThumbRippleAnim.apply{
                startDelay = 500L
                doOnStart {
                    binding.tapThumbRippleContainer.apply{
                        x = binding.rangeProgress.startScreenPos + binding.rangeProgress.x - width /2
                        alpha = 1f
                    }
                }
                doOnEnd {
                    binding.tapThumbRippleContainer.alpha = 0f
                    binding.rangeProgress.tapStartThumb()
                    job5()
                }
            }.start()
        }

        job5 = {
            cancelAnims(videoPanelInitAnim)
            videoPanelInitAnim.apply{
                startDelay = 500L
                setIntValues(80, 190)
                duration = 2000L / 300L * 110

                doOnStart {
                    binding.videoPanelControl.alpha = 1f
                }
                doOnEnd{
                    binding.videoPanelControl.alpha = 0f
                    binding.rangeProgress.resetThumb()
                    job2()
                }
            }.start()
        }

        job1()
    }

    private fun startTapThumbBeforeAnim(){
        cancelAnims(
            tapThumbRippleAnim,
            videoPanelInitAnim
        )

        binding.apply{
            rangeProgress.resetThumb()
            tapThumbRippleContainer.alpha = 0f
        }

        var job1 = {}
        var job2 = {}
        var job3 = {}

        job1 = {
            val progress = binding.rangeProgress.progress.toInt()
            videoPanelInitAnim.apply{
                setIntValues(progress, 190)
                duration = 1000L / 300L * abs(progress - 190)

                doOnEnd {
                    job2()
                }
            }.start()
        }

        job2 = {
            cancelAnims(progressIndicatorIntroAnim)

            progressIndicatorIntroAnim.apply{
                doOnStart {
                    binding.progressIndicator.translationY = 0f
                    binding.videoPanelControl.alpha = 0f
                }
                doOnEnd { job3() }
            }.start()
        }

        job3 = {
            cancelAnims(progressIndicatorIdleAnim)

            progressIndicatorIdleAnim.apply{
                doOnEnd {
                    binding.progressIndicator.alpha = 0f
                }
            }.start()
        }

        job1()
    }

    inner class TutorialTextAdapter : BaseAdapter(){
        init{
            set(arrayListOf(
                getString(R.string.dialog_tutorial_editor_video_panel),
                getString(R.string.dialog_tutorial_editor_progress_indicator),
                getString(R.string.dialog_tutorial_editor_tap_thumb)
            ))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return TutorialTextHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is TutorialTextHolder)
                holder.bind(list[position] as String)
        }
    }

    inner class TutorialTextHolder(parent:ViewGroup) : BaseViewHolder<String>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_tutorial_editor_text, parent, false)
    ){
        private var binding: ViewTutorialEditorTextBinding? = null

        private val explainAnim by lazy {
            val tX = dpToPixel(20)
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 600
                doOnStart {
                    binding?.explain?.translationX = tX
                }
                addUpdateListener {
                    val value = it.animatedValue as Float
                    binding?.explain?.apply{
                        translationX = if(value < 0.3f) (0.3f - value) * tX else 0f
                        alpha = value
                    }
                }
            }
        }

        override fun bind(data: String?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner
                text = data
            }

            currentPosition.observe(viewLifecycleOwner, Observer {
                if(it == adapterPosition) {
                    explainAnim.apply{
                        if(isRunning) cancel()
                    }.start()
                }
                else{
                    binding?.explain?.alpha = 0f
                }
            })
        }
    }
}