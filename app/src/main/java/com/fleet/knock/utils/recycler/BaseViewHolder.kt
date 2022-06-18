package com.fleet.knock.utils.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(protected val view: View) : RecyclerView.ViewHolder(view){

    abstract fun bind(data:T?)
}