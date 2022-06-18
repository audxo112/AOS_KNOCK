package com.fleet.knock.info.diff

import androidx.recyclerview.widget.DiffUtil
import com.fleet.knock.info.theme.FTheme
import com.fleet.knock.info.theme.FThemeLocal

class FThemeDiffCallback : DiffUtil.ItemCallback<FTheme>(){
    override fun areItemsTheSame(oldItem: FTheme, newItem: FTheme): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FTheme, newItem: FTheme): Boolean {
        return oldItem.id == newItem.id
    }
}