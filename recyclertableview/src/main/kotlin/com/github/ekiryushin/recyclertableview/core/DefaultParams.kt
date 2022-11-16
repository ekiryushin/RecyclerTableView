package com.github.ekiryushin.recyclertableview.core

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import io.github.ekiryushin.recyclertableview.R

/** Значения параметров по умолчанию */
object DefaultParams {

    /**
     * Получить цвет разделительных линий.
     */
    @ColorRes
    fun dividerColorResId(): Int = R.color.default_divider_color

    /**
     * Получить размер разделительных линий.
     */
    @DimenRes
    fun dividerSize(): Int = R.dimen.default_divider_size
}