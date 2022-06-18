package com.fleet.knock.ui.page.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.FragmentMainPromotionBinding
import com.fleet.knock.databinding.ViewMainPromotionBinding
import com.fleet.knock.info.promotion.FPromotion
import com.fleet.knock.ui.fragment.CompatFragment
import com.fleet.knock.ui.page.promotion.PromotionActivity
import com.fleet.knock.ui.page.theme_list.ThemeListRecentActivity
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SpaceVerticalDivider
import kotlinx.coroutines.*

class MainPromotionFragment : CompatFragment("Promotion"){
    private lateinit var binding:FragmentMainPromotionBinding

    private val viewModel:MainPromotionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_promotion, container, false)
        binding.apply{
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        binding.recentEffect.setOnClickListener {
            gotoRecent()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launch {
            binding.promotionList.apply{
                adapter = PromotionAdapter().apply{
                    set(viewModel.getPromotionList())
                }
                addItemDecoration(
                    SpaceVerticalDivider(0,
                        dpToPixel(40),
                        dpToPixel(40)
                    )
                )
            }
        }
    }

    private fun gotoRecent(){
        context ?: return

        startActivity(
            Intent(requireContext(), ThemeListRecentActivity::class.java)
        )
    }

    private fun gotoPromotion(promotionId:String){
        context ?: return
        if(promotionId == "")
            return

        startActivity(
            Intent(requireContext(), PromotionActivity::class.java).apply{
                putExtra(PromotionActivity.EXTRA_PROMOTION_ID, promotionId)
            }
        )
    }

    inner class PromotionAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return PromotionHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if (holder is PromotionHolder && list[position] is FPromotion)
                holder.bind(list[position] as FPromotion)
        }
    }

    inner class PromotionHolder(parent:ViewGroup) : BaseViewHolder<FPromotion>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_main_promotion, parent, false)
    ){
        private var binding : ViewMainPromotionBinding? = null

        private val viewModel:MainPromotionHolderViewModel by lazy{
            ViewModelProvider(this@MainPromotionFragment).get(
                toString(),
                MainPromotionHolderViewModel::class.java
            )
        }

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.Main + job)

        override fun bind(data: FPromotion?) {
            viewModel.bind(data)

            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel

                banner.setImageDrawable(null)
            }

            binding?.bannerEffect?.setOnClickListener {
                gotoPromotion(viewModel.promotionId)
            }

            binding?.gotoPromotion?.setOnClickListener {
                gotoPromotion(viewModel.promotionId)
            }

            bindBanner(data)
        }

        private fun bindBanner(promotion:FPromotion?){
            promotion?: return
            val img = binding?.banner ?: return
            img.setImageDrawable(null)

            scope.launch {
                Glide.with(img)
                    .load(loadBanner(promotion))
                    .into(img)
            }
        }

        private suspend fun loadBanner(promotion:FPromotion?) = withContext(Dispatchers.IO + job){
            context ?: return@withContext  null
            promotion ?: return@withContext null
            if(!promotion.updateBanner)
                viewModel.bannerDownload(promotion)
            promotion.getBanner(requireContext())
        }

    }

}