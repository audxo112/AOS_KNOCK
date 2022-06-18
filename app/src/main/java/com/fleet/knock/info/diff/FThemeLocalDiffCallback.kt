package com.fleet.knock.info.diff

import androidx.recyclerview.widget.DiffUtil
import com.fleet.knock.info.theme.FThemeLocal

class FThemeLocalDiffCallback : DiffUtil.ItemCallback<FThemeLocal>(){
    override fun areItemsTheSame(oldItem: FThemeLocal, newItem: FThemeLocal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FThemeLocal, newItem: FThemeLocal): Boolean {
        return oldItem.id == newItem.id
    }
}