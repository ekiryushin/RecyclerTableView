package com.github.ekiryushin.recyclertableview

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.ekiryushin.recyclertableview.R
import com.github.ekiryushin.recyclertableview.core.DefaultParams
import com.github.ekiryushin.recyclertableview.core.LogLevel
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils
import com.github.ekiryushin.recyclertableview.ui.LineItemDecoration

/**
 * Кастомная реализация [RecyclerView] для таблицы
 */
class RecyclerTableView(context: Context,
                        attrs: AttributeSet?,
                        defStyle: Int): RecyclerView(context, attrs, defStyle) {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RecyclerTableView)

        //Цвет разделительных линий
        val dividerColor = array.getColor(
            R.styleable.RecyclerTableView_divider_color,
            ContextCompat.getColor(context, DefaultParams.dividerColorResId()))

        //Размер разделительных линий
        val dividerSize = array.getDimensionPixelSize(
            R.styleable.RecyclerTableView_divider_size,
            resources.getDimensionPixelSize(DefaultParams.dividerSize()))

        array.recycle()

        addItemDecoration(LineItemDecoration(dividerSize, dividerColor))
    }

    /**
     * Сбросить все вычисленные параметры в [TableLayoutManager].
     * Вызывать в случаях, когда меняется DataSource.
     */
    fun refresh() {
        val tableLayoutManager = layoutManager
        if (tableLayoutManager !is TableLayoutManager) {
            RecyclerTableViewUtils.toLog(LogLevel.ERROR, "layoutManager должен быть TableLayoutManager")
        }
        else {
            removeAllViewsInLayout()
            tableLayoutManager.initParams()
            invalidate()
        }
    }
}