package com.github.ekiryushin.recyclertableview.ui.adapter

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils

/**
 * Адаптер динамических данных для ячеек таблицы.
 * @param countColumns количество столбцов должно быть одинаковым во всех строках.
 * @param diffUtil реализация сравнения элементов.
 */
abstract class RecyclerTableViewPagingAdapter<C: Any>(private val countColumns: Int,
                                                      diffUtil: DiffUtil.ItemCallback<C>):
    PagingDataAdapter<C, RecyclerTableViewHolder<C>>(diffUtil) {

    override fun onBindViewHolder(holder: RecyclerTableViewHolder<C>, position: Int) {
        val cellData: C? = getItem(position)
        val cell = RecyclerTableViewUtils.positionToCell(position, countColumns)
        holder.onBind(cellData, cell.first, cell.second)
    }
}