package com.github.ekiryushin.recyclertableview.example.data

import com.github.ekiryushin.recyclertableview.example.utils.StringUtils

/**
 * Данные для таблицы
 */
class TableSource {

    /**
     * Сформировать данные для отображения в таблице.
     * @param rowStart начальный номер строки для таблицы.
     * @param rowCount количество строк в таблице.
     * @param columnCount количество столбцов в таблице.
     * @return список строк в таблице, в каждой строке находится список значений для столбцов таблицы.
     */
    fun getData(rowStart: Int, rowCount: Int, columnCount: Int): List<List<String>> {
        val result: MutableList<List<String>> = mutableListOf()

        for (row in rowStart until rowStart + rowCount) {
            val columns: MutableList<String> = mutableListOf()
            for (column in 0 until columnCount) {
                //сделаем многострочный текст в некоторых строках и столбцах таблицы
                val countLine = when {
                    row == 1 && column == 15 -> 3
                    row == 2 && column == 1 -> 2
                    row == 5 && column == 2 -> 1
                    else -> 0
                }
                val str = StringBuilder()
                for (line in 0 until countLine) {
                    str.append("\n").append(StringUtils.getRandomText(15))
                }

                //начальное значение для ячеек (номер строки и столбца)
                var startText = "[$row, $column]: "
                var text: String
                when {
                    //значение для верхней левой ячейки
                    row == 0 && column == 0 -> {
                        startText = ""
                        text = "      Столбцы\nСтроки"
                    }
                    //значение для верхней строки
                    row == 0 -> {
                        startText = ""
                        text = "#$column"
                    }
                    //значение для левого столбца
                    column == 0 -> {
                        startText = ""
                        text = "#$row"
                    }
                    //значение для 1-го столбца на 30-ой строке
                    row == 30 && column == 1 -> text = StringUtils.getRandomText(30)
                    //все остальные значения
                    else -> text = StringUtils.getRandomText(15)
                }
                columns.add("$startText$text$str")
            }
            result.add(columns)
        }

        return result
    }
}