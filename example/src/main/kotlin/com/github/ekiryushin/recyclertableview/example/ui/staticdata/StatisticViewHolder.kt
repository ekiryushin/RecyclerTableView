package com.github.ekiryushin.recyclertableview.example.ui.staticdata

import android.graphics.Typeface
import com.github.ekiryushin.recyclertableview.ui.adapter.RecyclerTableViewHolder
import io.github.ekiryushin.recyclertableview.example.R
import io.github.ekiryushin.recyclertableview.example.databinding.CellItemBinding

class StatisticViewHolder(private val binding: CellItemBinding):
    RecyclerTableViewHolder<String>(binding.root) {

    override fun onBind(cell: String?, row: Int, column: Int) {
        binding.cellValue.text = cell

        //отдельный цвет для заголовка и чередование цветов в остальных строках таблицы
        val colorId = when {
            row == 0 || column == 0 -> R.color.row_header_background
            row % 2 == 0 -> R.color.row_even_background
            else -> R.color.row_odd_background
        }
        binding.root.setBackgroundResource(colorId)

        //жирный текст в четных столбцах
        val type = if (column % 2 == 0) Typeface.BOLD else Typeface.NORMAL
        binding.cellValue.typeface = Typeface.defaultFromStyle(type)
    }
}