package com.fleet.knock.ui.page.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.FragmentGalleryVideoBinding
import com.fleet.knock.databinding.ViewGalleryVideoCreatedItemBinding
import com.fleet.knock.databinding.ViewGalleryVideoItemBinding
import com.fleet.knock.info.gallery.Video
import com.fleet.knock.info.gallery.VideoBundle
import com.fleet.knock.ui.fragment.CompatFragment
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SectionSpaceGridDivider
import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.system.measureTimeMillis

class GalleryVideoFragment : CompatFragment("GalleryVideo"){
    private lateinit var binding: FragmentGalleryVideoBinding
    private val viewModel:GalleryVideoViewModel by viewModels()

    private var delegate: GotoEditorDelegate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery_video, container, false)
        binding.apply{
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        binding.videoList.apply{
            adapter = VideoAdapter()
            addItemDecoration(SectionSpaceGridDivider(dpToPixel(16), dpToPixel(3), dpToPixel(40), dpToPixel(3)))
            layoutManager = GridLayoutManager(requireContext(), 3).apply{
                spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        return adapter?.let{
                            if(it is VideoAdapter){
                                if(it.getItemViewType(position) == TYPE_SECTION) 3
                                else 1
                            }
                            else 1
                        } ?: 1
                    }
                }
            }
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is GotoEditorDelegate)
            delegate = context
        else
            throw RuntimeException("must implement GotoEditorDelegate")
    }

    inner class VideoAdapter : BaseAdapter(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return if(viewType == TYPE_SECTION){
                VideoCreatedHolder(parent)
            } else {
                VideoHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is VideoCreatedHolder){
                holder.bind(getSection(position))
            }
            else if(holder is VideoHolder){
                holder.bind(getVideo(position))
            }
        }

        override fun getItemViewType(position: Int): Int {
            var currentOffset = 0
            for(item in list){
                val bundle = item as VideoBundle
                if(currentOffset == position){
                    return TYPE_SECTION
                }
                else if(position > currentOffset &&
                    position <= bundle.list.size + currentOffset){
                    return TYPE_ITEM
                }
                currentOffset += bundle.list.size + 1
            }
            return TYPE_ITEM
        }

        private fun getSection(position:Int) : String{
            var currentOffset = 0
            for(item in list){
                val bundle = item as VideoBundle
                if(currentOffset == position){
                    return bundle.dateStr
                }
                else if(currentOffset > position){
                    return ""
                }
                currentOffset += bundle.list.size + 1
            }
            return ""
        }

        private fun getVideo(position:Int) : Video?{
            var currentOffset = 0
            for(item in list){
                val bundle = item as VideoBundle
                if(position > currentOffset &&
                    position <= bundle.list.size + currentOffset){
                    return bundle.list[position - currentOffset - 1]
                }
                else if(currentOffset > position){
                    return null
                }
                currentOffset += bundle.list.size + 1
            }
            return null
        }

        override fun getItemCount(): Int {
            var count = 0
            for(item in list){
                count += (item as VideoBundle).list.size + 1
            }
            return count
        }

    }

    inner class VideoCreatedHolder(parent:ViewGroup) : BaseViewHolder<String>(
        LayoutInflater.from(requireContext()).inflate(R.layout.view_gallery_video_created_item, parent, false)
    ){
        private var binding: ViewGalleryVideoCreatedItemBinding? = null

        override fun bind(data: String?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner
                created = data
            }
        }
    }

    inner class VideoHolder(parent: ViewGroup) : BaseViewHolder<Video>(
        LayoutInflater.from(requireContext()).inflate(R.layout.view_gallery_video_item, parent, false)
    ){
        private var binding: ViewGalleryVideoItemBinding? = null

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.Main + job)

        override fun bind(data: Video?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply{
                lifecycleOwner = viewLifecycleOwner
                video = data

                thumbnailEffect.setOnClickListener {
                    delegate?.gotoEditor(data)
                }
            }

            bindThumbnail(data)
            bindDuration(data)
        }

        private fun bindThumbnail(video:Video?){
            video?:return
            context?:return
            val img = binding?.thumbnail ?: return
            img.setImageDrawable(null)

            scope.launch {
                val bitmap = loadThumbnail(video) ?: return@launch
                if(video.id != binding?.video?.id){
                    bindThumbnail(binding?.video)
                    return@launch
                }

                try{
                    Glide.with(img)
                        .load(bitmap)
                        .into(img)
                }
                catch (e:IllegalStateException){
                    return@launch
                }catch (e:IllegalArgumentException){
                    return@launch
                }
            }
        }

        private fun bindDuration(video:Video?){
            video?:return
            context?:return

            scope.launch {
                val duration = loadDuration(video) ?: return@launch
                if(video.id != binding?.video?.id){
                    bindDuration(binding?.video)
                    return@launch
                }

                binding?.duration?.text = duration
            }
        }

        private suspend fun loadThumbnail(video:Video?) = withContext(Dispatchers.IO + job){
            try {
                video?.getThumbnail(requireContext())
            }
            catch (e:IllegalStateException){
                null
            }
        }

        private suspend fun loadDuration(video:Video?) = withContext(Dispatchers.IO + job){
            try {
                video?.getDurationStr(requireContext())
            }
            catch (e:IllegalStateException){
                null
            }
        }
    }

    companion object{
        const val TYPE_SECTION = 1
        const val TYPE_ITEM = 2
    }
}