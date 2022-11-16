package com.github.ekiryushin.recyclertableview.core

import android.util.Log

object RecyclerTableViewUtils {

    /** Уровень логирования библиотеки */
    var logLevel = LogLevel.DEBUG

    /**
     * Получить порядковые номера позиций каждого элемента для таблицы.
     * @param data список строк для таблицы. Каждая строка должна содержать в себе список значений
     * для каждого столбца таблицы.
     * @return мапа. На первом месте позиция, которая будет использоваться в адаптере,
     * на втором - пара, содержащая номер строки и столбца соответствующей ячейки в таблице.
     */
    fun <C> getAdapterPositions(data: List<List<C>>): Map<Int, Pair<Int, Int>> {
        var position = 0
        val result: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()

        data.forEachIndexed { row, columns ->
            columns.forEachIndexed { column, _ ->
                result[position++] = Pair(row, column)
            }
        }

        return result
    }

    /**
     * Преобразовать положение элемента в адаптере к строке и ширине этого элемента в таблице.
     * @param position позиция элемента в адаптере.
     * @param countColumns количество столбцов в таблице.
     * @return пара, содержащая номер строки и столбца соответствующей ячейки в таблице.
     */
    fun positionToCell(position: Int, countColumns: Int): Pair<Int, Int> =
        Pair(position / countColumns, position % countColumns)

    /**
     * Записать строку в лог.
     * @param text строка для логирования.
     */
    fun toLog(level: LogLevel, text: String) {
        if (logLevel.lvl >= level.lvl) {
            when (level) {
                LogLevel.INFO -> Log.i(Constants.LOG_TAG, text)
                LogLevel.ERROR -> Log.e(Constants.LOG_TAG, text)
                else -> Log.d(Constants.LOG_TAG, text)
            }
        }
    }
}