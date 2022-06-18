package com.fleet.knock.ui.page.gallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.fleet.knock.R
import com.fleet.knock.databinding.FragmentGalleryGifBinding
import com.fleet.knock.databinding.ViewGalleryGifCreatedItemBinding
import com.fleet.knock.databinding.ViewGalleryGifItemBinding
import com.fleet.knock.info.gallery.GifImage
import com.fleet.knock.info.gallery.GifImageBundle
import com.fleet.knock.ui.fragment.CompatFragment
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.BaseViewHolder
import com.fleet.knock.utils.recycler.SectionSpaceGridDivider
import kotlinx.coroutines.*
import java.lang.RuntimeException

class GalleryGifFragment : CompatFragment("GalleryGif") {
    private lateinit var binding: FragmentGalleryGifBinding
    private val viewModel: GalleryGifViewModel by viewModels()

    private var delegate: GotoEditorDelegate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery_gif, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        binding.gifList.apply{
            adapter = GifAdapter()
            addItemDecoration(SectionSpaceGridDivider(dpToPixel(16), dpToPixel(3), dpToPixel(40), dpToPixel(3)))
            layoutManager = GridLayoutManager(requireContext(), 3).apply{
                spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        return adapter?.let{
                            if(it is GifAdapter){
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

        if (context is GotoEditorDelegate)
            delegate = context
        else
            throw RuntimeException("must implement GotoEditorDelegate")
    }

    inner class GifAdapter : BaseAdapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            return if(viewType == TYPE_SECTION){
                GifCreatedHolder(parent)
            }
            else{
                GifHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            if(holder is GifCreatedHolder){
                holder.bind(getSection(position))
            }
            else if(holder is GifHolder){
                holder.bind(getGifImage(position))
            }
        }

        override fun getItemViewType(position: Int): Int {
            var currentOffset = 0
            for (item in list) {
                val bundle = item as GifImageBundle
                if (currentOffset == position) {
                    return TYPE_SECTION
                } else if (position > currentOffset &&
                    position <= bundle.list.size + currentOffset
                ) {
                    return TYPE_ITEM
                }
                currentOffset += bundle.list.size + 1
            }
            return TYPE_ITEM
        }

        private fun getSection(position: Int): String {
            var currentOffset = 0
            for (item in list) {
                val bundle = item as GifImageBundle
                if (currentOffset == position) {
                    return bundle.dateStr
                } else if (currentOffset > position) {
                    return ""
                }
                currentOffset += bundle.list.size + 1
            }
            return ""
        }

        private fun getGifImage(position: Int): GifImage? {
            var currentOffset = 0
            for (item in list) {
                val bundle = item as GifImageBundle
                if (position > currentOffset &&
                    position <= bundle.list.size + currentOffset
                ) {
                    return bundle.list[position - currentOffset - 1]
                } else if (currentOffset > position) {
                    return null
                }
                currentOffset += bundle.list.size + 1
            }
            return null
        }

        override fun getItemCount(): Int {
            var count = 0
            for (item in list) {
                count += (item as GifImageBundle).list.size + 1
            }
            return count
        }
    }

    inner class GifCreatedHolder(parent: ViewGroup) : BaseViewHolder<String>(
        LayoutInflater.from(requireContext())
            .inflate(R.layout.view_gallery_gif_created_item, parent, false)
    ) {
        private var binding: ViewGalleryGifCreatedItemBinding? = null

        override fun bind(data: String?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply {
                lifecycleOwner = viewLifecycleOwner
                created = data
            }
        }
    }

    inner class GifHolder(parent: ViewGroup) : BaseViewHolder<GifImage>(
        LayoutInflater.from(requireContext()).inflate(R.layout.view_gallery_gif_item, parent, false)
    ) {
        private var binding: ViewGalleryGifItemBinding? = null

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.Main + job)

        override fun bind(data: GifImage?) {
            binding = DataBindingUtil.bind(view)
            binding?.apply {
                lifecycleOwner = viewLifecycleOwner
                gif = data

                thumbnailEffect.setOnClickListener {
                    delegate?.gotoEditor(data)
                }
            }

            bindThumbnail(data)
        }

        private fun bindThumbnail(gif:GifImage?){
            context?: return
            gif?:return
            val img = binding?.thumbnail ?: return
            img.setImageDrawable(null)

            scope.launch {
                val bitmap = loadThumbnail(gif) ?: return@launch
                if(gif.id != binding?.gif?.id){
                    bindThumbnail(binding?.gif)
                    return@launch
                }
                try {
                    Glide.with(img)
                        .load(bitmap)
                        .into(img)
                }catch (e:IllegalStateException){
                    return@launch
                }catch (e:IllegalArgumentException){
                    return@launch
                }
            }
        }

        private suspend fun loadThumbnail(gif: GifImage?) = withContext(Dispatchers.IO + job){
            try {
                gif?.getThumbnail(requireContext())
            }catch (e:IllegalStateException){
                null
            }
        }
    }
    companion object {
        const val TYPE_SECTION = 1
        const val TYPE_ITEM = 2
    }
}