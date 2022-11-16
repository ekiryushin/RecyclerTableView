package com.github.ekiryushin.recyclertableview.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Связь данных с конкретной ячейкой таблицы.
 */
abstract class RecyclerTableViewHolder<C>(itemView: View): RecyclerView.ViewHolder(itemView) {

    /**
     * Привязать данные к конкретной ячейке таблицы.
     * @param cell данные для ячейки.
     * @param row строка ячейки для привязки данных.
     * @param column столбец ячейки для привязки данных.
     */
    abstract fun onBind(cell: C?, row: Int, column: Int)
}