package com.github.ekiryushin.recyclertableview.example.ui.staticdata

import com.github.ekiryushin.recyclertableview.example.data.TableSource

class StatisticPresenter(private val source: TableSource) {

    companion object {
        /** Количество строк в таблице */
        private const val COUNT_ROW = 101

        /** Количество столбцов в таблице */
        private const val COUNT_COLUMN = 21
    }

    /**
     * Сформировать данные для отображения в таблице.
     * @return список строк в таблице, в каждой строке находится список значений для столбцов таблицы.
     */
    fun getData(): List<List<String>> = source.getData(0, COUNT_ROW, COUNT_COLUMN)
}