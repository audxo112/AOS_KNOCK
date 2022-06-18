package com.fleet.knock.utils.recycler

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter : RecyclerView.Adapter<BaseViewHolder<*>>(){
    val list = mutableListOf<Any?>()

    override fun getItemCount(): Int {
        return list.size
    }

    fun isEmpty() = list.isEmpty()

    fun addAll(l:List<Any?>?){
        l?:return

        list.addAll(l)

        notifyItemRangeInserted(list.size - l.size, l.size)
    }

    fun set(l:List<Any?>?){
        l?:return
        list.clear()
        list.addAll(l)
        notifyDataSetChanged()
    }

    fun set(s:Set<Any?>?){
        s?: return
        list.clear()
        list.addAll(s)
        notifyDataSetChanged()
    }

    fun delete(position:Int){
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}