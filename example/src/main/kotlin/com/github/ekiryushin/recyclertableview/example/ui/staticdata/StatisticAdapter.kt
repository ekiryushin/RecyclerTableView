package com.github.ekiryushin.recyclertableview.example.ui.staticdata

import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.ekiryushin.recyclertableview.example.databinding.CellItemBinding
import com.github.ekiryushin.recyclertableview.ui.adapter.RecyclerTableViewAdapter
import com.github.ekiryushin.recyclertableview.ui.adapter.RecyclerTableViewHolder

class StatisticAdapter(data: List<List<String>>): RecyclerTableViewAdapter<String>(data) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerTableViewHolder<String>
    {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellItemBinding.inflate(inflater, parent, false)
        return StatisticViewHolder(binding)
    }
}