package com.fleet.knock.ui.page.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.fleet.knock.R
import com.fleet.knock.databinding.FragmentMainHomeBinding
import com.fleet.knock.databinding.ViewMainHomeLocalThemeItemBinding
import com.fleet.knock.databinding.ViewMainHomeLocalThemeMoreBinding
import com.fleet.knock.info.theme.FThemeLocal
import com.fleet.knock.ui.dialog.ApplyDialog
import com.fleet.knock.ui.fragment.CompatFragment
import com.fleet.knock.ui.page.gallery.GalleryActivity
import com.fleet.knock.ui.page.preview.PreviewLocalActivity
import com.fleet.knock.ui.page.theme_list.ThemeListLocalActivity
import com.fleet.knock.utils.StorageUtil
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SpaceHorizontalDivider

class MainHomeFragment : CompatFragment("Home"){
    private lateinit var binding : FragmentMainHomeBinding
    private val viewModel:MainHomeViewModel by viewModels()

    private val applyDialog by lazy{
        ApplyDialog()
    }

    private var selectedLocalThemeId:Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_home, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.apply{
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel

            myVideoTheme.setOnClickListener{
                gotoGallery()
            }
        }

        binding.localThemeList.apply{
            adapter = LocalThemeAdapter()
            addItemDecoration(
                SpaceHorizontalDivider(
                    dpToPixel(20),
                    dpToPixel(20),
                    dpToPixel(6)
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_PICK_THEME && resultCode == Activity.RESULT_OK){
            showApplyDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        context?:return

        if(requestCode == REQUEST_PERMISSION_GALLERY && StorageUtil.isPermission(requireContext())){
            gotoGallery()
        }
        else if(requestCode == REQUEST_PERMISSION_LOCAL_THEME && StorageUtil.isPermission(requireContext())){
            gotoLocalPreview(selectedLocalThemeId)
        }
    }

    private fun showApplyDialog(){
        if(!applyDialog.isAdded)
            applyDialog.show(childFragmentManager, "Apply")
    }

    private fun gotoGallery(){
        context?:return
        activity ?: return
        if(requireActivity().isFinishing) return

        if(StorageUtil.isPermission(requireContext())) {
            startActivityForResult(
                Intent(requireContext(), GalleryActivity::class.java)
                , REQUEST_PICK_THEME
            )
        }
        else{
            StorageUtil.requestPermission(this, REQUEST_PERMISSION_GALLERY)
        }
    }

    private fun gotoLocalPreview(themeId: Long?) {
        themeId?: return
        context ?: return
        activity ?: return
        if(requireActivity().isFinishing) return
        selectedLocalThemeId = null

        if(StorageUtil.isPermission(requireContext())) {
            startActivity(
                Intent(requireContext(), PreviewLocalActivity::class.java).apply {
                    putExtra(PreviewLocalActivity.EXTRA_THEME_ID, themeId)
                }
            )
            requireActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_not_move)
        }
        else{
            selectedLocalThemeId = themeId
            StorageUtil.requestPermission(this, REQUEST_PERMISSION_LOCAL_THEME)
        }
    }

    private fun gotoLocalMore() {
        context ?: return
        
        startActivity(
            Intent(requireContext(), ThemeListLocalActivity::class.java)
        )
    }

    inner class LocalThemeAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return if(viewType == TYPE_LOCAL_THEME)
                LocalThemeHolder(parent)
            else
                LocalMoreHolder(parent)
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is LocalThemeHolder)
                holder.bind(list[position] as FThemeLocal)
            else
                holder.bind(null)
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == itemCount - 1) TYPE_LOCAL_MORE
            else TYPE_LOCAL_THEME
        }

        override fun getItemCount(): Int {
            return list.size + 1
        }
    }

    inner class LocalThemeHolder(parent: ViewGroup) : BaseViewHolder<FThemeLocal?>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_main_home_local_theme_item, parent, false)
    ){
        private var binding : ViewMainHomeLocalThemeItemBinding? = null

        override fun bind(data: FThemeLocal?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner
                theme = data

                themeThumbnailEffect.setOnClickListener {
                    gotoLocalPreview()
                }
            }
        }

        private fun gotoLocalPreview(){
            val themeId = binding?.theme?.id ?: return

            gotoLocalPreview(themeId)
        }
    }

    inner class LocalMoreHolder(parent: ViewGroup) : BaseViewHolder<Any?>(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_main_home_local_theme_more, parent, false)
    ) {
        private var binding : ViewMainHomeLocalThemeMoreBinding? = null

        override fun bind(data: Any?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner

                more.setOnClickListener {
                    gotoLocalMore()
                }
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSION_GALLERY = 10001
        const val REQUEST_PERMISSION_LOCAL_THEME = 10002

        const val REQUEST_PICK_THEME = 3001

        const val TYPE_LOCAL_THEME = 0
        const val TYPE_LOCAL_MORE = 1
    }
}