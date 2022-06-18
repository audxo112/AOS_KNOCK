package com.fleet.knock.utils.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fleet.knock.info.provider.FileProvider
import com.fleet.knock.ui.view.FPlayerView
import com.fleet.knock.utils.recycler.BaseAdapter
import com.fleet.knock.utils.recycler.SpaceGridDivider
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IllegalArgumentException
import java.net.SocketException

open class BaseViewModel(application: Application) : AndroidViewModel(application){
    protected val config = PreferenceManager.getDefaultSharedPreferences(application)

    protected suspend fun resourceDownload(provider: FileProvider, onProgress:(p:String)->Unit = {}) = withContext(Dispatchers.IO){
        if(downloadSet.contains(provider.gsLink)) {
            launch(Dispatchers.IO) {
                while (downloadSet.contains(provider.gsLink)) {
                    delay(50)
                }
            }.join()
            return@withContext
        }

        try {
            val fs = FirebaseStorage.getInstance()
            val ref = fs.getReference(provider.gsLink)
//            val metadata = ref.metadata
//                .await()
//
//            if (provider.isValidFile(metadata.sizeBytes)) {
//                return@withContext
//            }

            downloadSet.add(provider.gsLink)

            ref.getFile(provider.file)
                .addOnProgressListener { s ->
                    onProgress("${s.bytesTransferred * 100 / s.totalByteCount}%")
                }
                .addOnCanceledListener {
                    Log.d("DOWNLOAD_ERROR", "canceled")
                }
                .addOnFailureListener {
                    Log.d("DOWNLOAD_ERROR", "message : ${it.message}")
                }
                .await()
        }
        catch(e:Exception){
            val crash = FirebaseCrashlytics.getInstance()
            crash.log("${e.message}, BaseViewModel resourceDownload : ${provider.gsLink}")
        }

        downloadSet.remove(provider.gsLink)
    }

    object Bind {
        @JvmStatic
        @BindingAdapter("viewHeight")
        fun setViewHeight(view:View, dp:Int){
            view.layoutParams = view.layoutParams.apply{
                height = dpToPixel(view.context, dp)
            }
        }

        @JvmStatic
        @BindingAdapter("recycleItem")
        fun setRecycleItem(view: RecyclerView, list:List<Any?>?){
            view.adapter ?: return
            if(view.adapter is BaseAdapter){
                (view.adapter as BaseAdapter).set(list)
            }
        }

        @JvmStatic
        @BindingAdapter("listItem")
        fun setListItem(view: RecyclerView, list:List<Any>?){
            view.adapter ?: return
            if(view.adapter is ListAdapter<*, *>){
                (view.adapter as ListAdapter<Any, *>).submitList(list)
            }
        }

        @JvmStatic
        @BindingAdapter("gridDivider")
        fun setGridDivider(view:RecyclerView, dp:Int){
            view.addItemDecoration(
                SpaceGridDivider(
                    dpToPixel(
                        view.context,
                        dp
                    )
                )
            )
        }

        @JvmStatic
        @BindingAdapter("setTag")
        fun setTag(view:View, tag:String){
            view.tag = tag
        }

        @JvmStatic
        @BindingAdapter("clipToOutline")
        fun clipToOutline(view:ImageView, clip:Boolean){
            view.clipToOutline = clip
        }

        @JvmStatic
        @BindingAdapter("imgRes")
        fun imgRes(view: ImageView, resId: Int) {
            if(resId == 0) {
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .load(resId)
                    .into(view)
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("imgBitmap")
        fun imgBitmap(view: ImageView, bitmap: Bitmap?) {
            if(bitmap == null){
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .load(bitmap)
                    .into(view)
            } catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("imgFile")
        fun imgFile(view: ImageView, file: File?) {
            if(file == null){
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .load(file)
                    .centerCrop()
                    .into(view)
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("imgDrawable")
        fun imgDrawable(view: ImageView, drawable: Drawable?) {
            if(drawable == null){
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .load(drawable)
                    .into(view)
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("circleImgBitmap")
        fun circleImgBitmap(view: ImageView, bitmap: Bitmap?) {
            if(bitmap == null){
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .asBitmap()
                    .load(bitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("circleImgDrawable")
        fun circleImgDrawable(view: ImageView, drawable: Drawable?) {
            if(drawable == null){
                view.setImageDrawable(null)
                return
            }

            try {
                Glide.with(view)
                    .load(drawable)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("backgroundRes")
        fun backgroundRes(view: View, backgroundResId:Int){
            view.setBackgroundResource(backgroundResId)
        }

        @JvmStatic
        @BindingAdapter("textStyleValue")
        fun textStyleValue(view:TextView, style:Int){
            view.setTypeface(view.typeface, style)
        }

        @JvmStatic
        @BindingAdapter("textColorRes")
        fun textColorRes(view: TextView, colorResId:Int){
            view.setTextColor(ContextCompat.getColor(view.context, colorResId))
        }

        @JvmStatic
        @BindingAdapter("loadVideo")
        fun loadVideo(playerView: PlayerView, dataSpec: DataSpec?){

            val player = playerView.player?.let{
                if(it is SimpleExoPlayer) it else null
            } ?: return
            dataSpec?:return
            try {
                val beforePlayWhenReady = player.playWhenReady
                player.playWhenReady = false
                player.stop(true)
                player.prepare(FileDataSource().let { dataSource ->
                    dataSource.open(dataSpec)
                    DataSource.Factory { dataSource }.let { factory ->
                        ProgressiveMediaSource.Factory(factory).createMediaSource(dataSource.uri)
                    }
                })
                player.playWhenReady = beforePlayWhenReady
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("loadVideoSource")
        fun loadVideoSource(playerView: FPlayerView, source:ProgressiveMediaSource?){
            val player = playerView.player as SimpleExoPlayer? ?: return
            source ?: return

            try{
                player.apply{
                    val beforePlayWhenReady = playWhenReady
                    stop()
                    prepare(source)
                    playWhenReady = beforePlayWhenReady
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

        @JvmStatic
        @BindingAdapter("resizeMode")
        fun resizeMode(playerView:PlayerView, resize:Int){
            playerView.resizeMode = resize
        }

        @JvmStatic
        @BindingAdapter("preventTouch")
        fun preventScroll(view:View, prevent:Boolean){
            if(prevent){
                view.setOnTouchListener { _, _ ->
                    return@setOnTouchListener true
                }
            }
            else{
                view.setOnTouchListener(null)
            }
        }

        @JvmStatic
        @BindingAdapter("layoutRatio")
        fun setLayoutRatio(view:View, ratio:String?){
            ratio?:return
            if(ratio == "") return
            val params = view.layoutParams
            if(params is ConstraintLayout.LayoutParams){
                params.dimensionRatio = ratio
            }
        }

        @JvmStatic
        @BindingAdapter("visibility")
        fun setVisibility(view:View, visibility:Int?){
            visibility ?: return

            view.visibility = visibility
        }

        @JvmStatic
        @BindingAdapter("layoutTopMarginWithStatus")
        fun setLayoutTopMarginWithStatusBar(view:View, dp:Int){
            (view.layoutParams as ViewGroup.MarginLayoutParams).also{
                it.topMargin = dpToPixel(
                    view.context,
                    dp
                ) + getStaHeight(
                    view.context
                )
                view.layoutParams = it
            }
        }

        fun getStaHeight(context:Context) : Int{
            val statusBarId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            return if(statusBarId > 0) context.resources.getDimension(statusBarId).toInt()
            else 0
        }

        @JvmStatic
        @BindingAdapter("layoutBottomMarginWithNav")
        fun setLayoutBottomMarginWithNavigation(view:View, dp:Int){
            (view.layoutParams as ViewGroup.MarginLayoutParams).also {
                it.bottomMargin = dpToPixel(
                    view.context,
                    dp
                ) + getNavHeight(
                    view.context
                )
                view.layoutParams = it
            }
        }

        fun getNavHeight(context:Context) : Int{
            val showNavId = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
            val showNav = if(showNavId > 0) context.resources.getBoolean(showNavId)
            else ViewConfiguration.get(context).hasPermanentMenuKey()

            return if(showNav){
                val navHeightId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
                if(navHeightId > 0) context.resources.getDimension(navHeightId).toInt()
                else 0
            }
            else 0
        }
    }

    companion object{
        fun dpToPixel(context: Context, dp:Int) : Int{
            return context.resources.displayMetrics.let{dm->
                (dp*((dm.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)).toInt()
            }
        }

        val downloadSet = HashSet<String>()
    }
}