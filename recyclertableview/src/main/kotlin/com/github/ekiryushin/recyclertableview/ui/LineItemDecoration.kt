package com.github.ekiryushin.recyclertableview.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.github.ekiryushin.recyclertableview.TableLayoutManager
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils

/**
 * Выделение границ элементов.
 * @param size Толщина линий.
 * @param color Цвет линий.
 */
class LineItemDecoration(size: Int,
                         color: Int): RecyclerView.ItemDecoration() {
    private val paint = Paint()

    init {
        paint.color = color
        paint.strokeWidth = size.toFloat()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        //получим количество столбцов в таблице
        var columnCounts = 0
        var fixHeader = false
        var fixColumn = false
        if (parent.layoutManager is TableLayoutManager) {
            columnCounts = (parent.layoutManager as TableLayoutManager).countColumns
            fixHeader = (parent.layoutManager as TableLayoutManager).fixHeader
            fixColumn = (parent.layoutManager as TableLayoutManager).fixColumn
        }

        val headerViews = mutableListOf<View>()
        val leftViews = mutableListOf<View>()
        var topLeftView: View? = null
        for (ind in 0 until parent.childCount) {
            val view = parent.getChildAt(ind)
            val left = view.left.toFloat()
            val right = view.right.toFloat()
            val top = view.top.toFloat()
            val bottom = view.bottom.toFloat()

            //формируем список ячеек шапки таблицы и левого столбца для закрепления
            if ((fixHeader || fixColumn) && columnCounts > 0) {
                val position = parent.getChildAdapterPosition(view)
                val cell = RecyclerTableViewUtils.positionToCell(position, columnCounts)
                val isFirstRow = cell.first == 0
                val isFirstColumn = cell.second == 0
                when {
                    fixHeader && fixColumn && isFirstRow && isFirstColumn -> topLeftView = view
                    fixHeader && isFirstRow -> headerViews.add(view)
                    fixColumn && isFirstColumn -> leftViews.add(view)
                }
            }

            //рисуем линию снизу и слева
            drawLines(canvas, right, bottom, left, top)
        }

        //закрепляем все ячейки
        fixedHeader(canvas, headerViews)
        fixedColumn(canvas, leftViews)
        fixedLeftTopCell(canvas, topLeftView)
    }

    /**
     * Закрепить шапку таблицы.
     * @param canvas Канвас экрана.
     * @param headerViews Список view ячеек шапки для закрепления.
     */
    private fun fixedHeader(canvas: Canvas, headerViews: MutableList<View>) {
        if (headerViews.isNotEmpty()) {
            headerViews.forEach {
                val bottom = it.height.toFloat()
                val right = it.width.toFloat()

                canvas.save()
                canvas.translate(it.left.toFloat(), 0f)
                it.draw(canvas)
                drawLines(canvas, right, bottom, 0f, 0f)
                canvas.drawLine(0f, bottom, right, bottom, paint)
                canvas.restore()
            }
        }
    }

    /**
     * Закрепить левый столбец таблицы.
     * @param canvas Канвас экрана.
     * @param leftViews Список view ячеек левого столбца для закрепления.
     */
    private fun fixedColumn(canvas: Canvas, leftViews: MutableList<View>) {
        if (leftViews.isNotEmpty()) {
            leftViews.forEach {
                val bottom = it.height.toFloat()
                val right = it.width.toFloat()

                canvas.save()
                canvas.translate(0f, it.top.toFloat())
                it.draw(canvas)
                drawLines(canvas, right, bottom, 0f, 0f)
                canvas.drawLine(right, bottom, right, 0f, paint)
                canvas.restore()
            }
        }
    }

    /**
     * Закрепить левую верхнюю ячейку таблицы.
     * @param canvas Канвас экрана.
     * @param topLeftView View девой верхней ячейки для закрепления.
     */
    private fun fixedLeftTopCell(canvas: Canvas, topLeftView: View?) {
        topLeftView?.let {
            val bottom = it.height.toFloat()
            val right = it.width.toFloat()

            canvas.save()
            canvas.translate(0f, 0f)
            it.draw(canvas)
            drawLines(canvas, right, bottom, 0f, 0f)
            canvas.drawLine(0f, bottom, right, bottom, paint)
            canvas.drawLine(right, bottom, right, 0f, paint)
            canvas.restore()
        }
    }

    /**
     * Нарисовать линию слева и сверху.
     * @param canvas Канвас экрана.
     * @param right координаты по оси X для правого края верхней линии.
     * @param bottom координаты по оси Y для окончания линии снизу.
     * @param left координаты по оси X для линии слева.
     * @param up координаты по оси Y для линии сверху.
     */
    private fun drawLines(canvas: Canvas, right: Float, bottom: Float, left: Float, up: Float) {
        canvas.drawLine(left, bottom, left, up, paint)
        canvas.drawLine(left, up, right, up, paint)
    }
}