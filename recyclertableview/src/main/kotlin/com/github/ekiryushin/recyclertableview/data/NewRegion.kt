package com.github.ekiryushin.recyclertableview.data

/**
 * Позиция на экране для новой добавляемой ячейки.
 * Может быть заполнено только одно из двух значений.
 */
data class NewRegion(
    /** Позиция элемента будет слева, сверху */
    val leftTop: Pair<Int, Int>? = null,

    /** Позиция элемента будет справа снизу */
    val rightBottom: Pair<Int, Int>? = null
)
