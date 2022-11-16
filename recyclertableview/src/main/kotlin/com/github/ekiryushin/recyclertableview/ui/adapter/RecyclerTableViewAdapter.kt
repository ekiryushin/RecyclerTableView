package com.github.ekiryushin.recyclertableview.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.github.ekiryushin.recyclertableview.core.LogLevel
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils
import com.github.ekiryushin.recyclertableview.exceptions.IncorrectAdapterPositionException
import com.github.ekiryushin.recyclertableview.exceptions.IncorrectColumnCountException

/**
 * Адаптер данных для ячеек таблицы.
 * @param data список строк для таблицы. Каждая строка должна содержать в себе список значений
 * для каждого столбца таблицы. __Важно! Количество столбцов должно быть одинаковым во всех строках__.
 * @throws IncorrectColumnCountException при выявлении разного числа столбцов в строках.
 * @throws IncorrectAdapterPositionException при обработке позиции ячейки, которой нет в таблице.
 */
abstract class RecyclerTableViewAdapter<C>(private val data: List<List<C>>):
    RecyclerView.Adapter<RecyclerTableViewHolder<C>>() {

    /** Количество столбцов в таблице */
    val countColumns: Int = if (data.isNotEmpty()) data[0].size else 0

    /** Позиции ячеек таблицы в адаптере */
    private val positions: Map<Int, Pair<Int, Int>>

    init {
        validateColumnsCount()
        positions = RecyclerTableViewUtils.getAdapterPositions(data)
    }

    @Throws(IncorrectAdapterPositionException::class)
    override fun onBindViewHolder(holder: RecyclerTableViewHolder<C>, position: Int) {
        val cell = getCell(position)
        val row = cell.first
        val column = cell.second

        holder.onBind(data[row][column], row, column)
    }

    override fun getItemCount(): Int = positions.size

    /**
     * Получить элемент по его позиции в адаптере
     * @param position позиция элемента в адаптере
     */
    @Throws(IncorrectAdapterPositionException::class)
    protected fun getItem(position: Int): C {
        val cell = getCell(position)
        val row = cell.first
        val column = cell.second
        return data[row][column]
    }

    /**
     * Получить координаты ячейки по ее позиции в адаптере
     * @param position позиция элемента в адаптере
     * @return пара: на первом месте номер строки ячейки, на втором - номер столбца
     */
    @Throws(IncorrectAdapterPositionException::class)
    protected fun getCell(position: Int): Pair<Int, Int> = positions[position]
        ?: throw IncorrectAdapterPositionException("Неизвестная позиция ячейки: $position")

    /**
     * Проверить корректность количества столбцов в каждой строке.
     * @throws IncorrectColumnCountException при выявлении разного числа столбцов с строках.
     */
    @Throws(IncorrectColumnCountException::class)
    private fun validateColumnsCount()  {
        val errors = StringBuilder()
        data.forEachIndexed { index, columns ->
            if (columns.isNotEmpty() && columns.size != countColumns) {
                errors.append("$index: ${columns.size}; ")
            }
        }

        if (errors.isNotEmpty()) {
            val err = "Количество столбцов в таблице: $countColumns. " +
                    "Строки с неверным количеством столбцов: $errors"
            RecyclerTableViewUtils.toLog(LogLevel.ERROR, err)
            throw IncorrectColumnCountException(err)
        }
    }
}