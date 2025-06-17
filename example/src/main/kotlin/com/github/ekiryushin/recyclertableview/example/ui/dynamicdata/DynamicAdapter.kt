package com.github.ekiryushin.recyclertableview.example.ui.dynamicdata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.github.ekiryushin.recyclertableview.example.databinding.CellItemBinding
import com.github.ekiryushin.recyclertableview.example.ui.staticdata.StatisticViewHolder
import com.github.ekiryushin.recyclertableview.ui.adapter.RecyclerTableViewHolder
import com.github.ekiryushin.recyclertableview.ui.adapter.RecyclerTableViewPagingAdapter

class DynamicAdapter(columnCount: Int): RecyclerTableViewPagingAdapter<String>(columnCount, diffUtil) {

    companion object {
        private val diffUtil = object: DiffUtil.ItemCallback<String>() {
            /**
             * Реализация полного сравнения.
             * @param oldItem предыдущий элемент для сравнения.
             * @param newItem новый элемент для сравнения.
             * @return true - сравниваемый элемент аналогичен текущему, false - элементы различаются.
             */
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            /**
             * Реализация частичного сравнения.
             * @param oldItem предыдущий элемент для сравнения.
             * @param newItem новый элемент для сравнения.
             * @return true - сравниваемый элемент аналогичен текущему, false - элементы различаются.
             */
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return when {
                    oldItem.isEmpty() && newItem.isEmpty() -> true
                    oldItem.isEmpty() || newItem.isEmpty() -> false
                    else -> oldItem[0] == newItem[0]
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerTableViewHolder<String>
    {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellItemBinding.inflate(inflater, parent, false)
        return StatisticViewHolder(binding)
    }
}