package com.github.ekiryushin.recyclertableview

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils
import com.github.ekiryushin.recyclertableview.data.NewRegion
import com.github.ekiryushin.recyclertableview.data.Rect
import com.github.ekiryushin.recyclertableview.exceptions.IncorrectViewSize
import kotlin.math.abs

/**
 * Менеджер компоновки элементов в виде таблицы. В памяти находятся view элементов в двух верхних
 * и двух нижних строках, в двух левых и двух правых столбцах.
 * @param context контекст приложения.
 * @param countColumns количество столбцов в таблице. Оно не меняется, а количество строк
 * может меняться.
 * @param fixHeader Закреплять или нет шапку таблицы.
 * @param fixColumn Закреплять или нет левый столбец таблицы.
 */
class TableLayoutManager(private val context: Context,
                         val countColumns: Int,
                         val fixHeader: Boolean = false,
                         val fixColumn: Boolean = false): RecyclerView.LayoutManager() {

    companion object {
        /**
         * Количество по умолчанию предварительно добавленных строк и столбцов, которые в скором
         * времени будут показаны на экране.
         */
        private const val DEFAULT_NUMBER_PRELOADED_ITEMS = 1
    }

    /**
     * Количество предварительно загруженных ячеек, которые в скором времени будут показаны
     * на экране. Это же количество используется для удаления ячеек с экрана,
     * которые уже не видны.
     *
     * Допустим подгружается по 3 ячейки. На экране сейчас показано 5 столбцов, пятый показан
     * частично. Как только этот пятый столбец показался на экране, произошла догрузка шестого,
     * седьмого и восьмого столбцов. Когда показался шестой столбец, подгрузился девятый.
     */
    private var numberPreloadedItems: Int = DEFAULT_NUMBER_PRELOADED_ITEMS

    /**
     * Максимальная ширина каждого столбца таблицы.
     * Ключ - это номер столбца, значение - это ширина столбца.
     */
    private var widthColumns: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Максимальная высота каждой строки таблицы.
     * Ключ - это номер строки, значение - это высота строки.
     */
    private var heightRows: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Левый и правый столбцы, верхняя и нижняя строки, элементы внутри которых добавлены
     * в данный момент на экран.
     * + [Rect.left] - номер видимого столбца слева.
     * + [Rect.top] - номер видимой строки сверху.
     * + [Rect.right] - номер видимого столбца справа.
     * + [Rect.bottom] - номер видимой строки снизу.
     */
    private var bindRowColumnCells = Rect()

    /**
     * Смещение таблицы на экране по оси X
     */
    private var xOffset = 0

    /**
     * Смещение таблицы на экране по оси Y
     */
    private var yOffset = 0

    /**
     * Ширина таблицы. Считается динамически при обновлении размера в [widthColumns]
     */
    private var tableWidth = 0

    /**
     * Высота таблицы. Считается динамически при обновлении размера в [heightRows]
     */
    private var tableHeight = 0

    /**
     * Сформировать параметры по умолчанию для каждого элемента
     * @return сформированные параметры
     */
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

    override fun canScrollHorizontally(): Boolean = true

    override fun canScrollVertically(): Boolean = true

    /**
     * Скролл влево и вправо.
     * @param dx количество пикселей для смещения.
     * [dx] < 0 -> скролл слева направо. [dx] > 0 -> скролл справа налево.
     */
    override fun scrollHorizontallyBy(dx: Int,
                                      recycler: RecyclerView.Recycler?,
                                      state: RecyclerView.State?): Int {
        var newDx = dx

        //скроллим слева направо, новые элементы будут показывать слева
        if (newDx < 0 && xOffset - newDx > 0) {
            newDx = xOffset
        }

        //скроллим справа налево, новые элементы будут справа
        if (newDx > 0 && tableWidth < width) {
            newDx = 0 //таблица меньше ширины экрана
        }
        else if (newDx > 0 && xOffset + tableWidth - newDx < width) {
            newDx = xOffset + tableWidth - width
        }

        if (newDx != 0) {
            offsetChildrenHorizontal(-newDx)
            xOffset += -newDx
            recycler?.let { processingAllItems(it) }
        }
        return newDx
    }

    /**
     * Скролл вниз и вверх.
     * @param dy количество пикселей для смещения.
     * [dy] < 0 -> скролл сверху вниз. [dy] > 0 -> скролл снизу вверх.
     */
    override fun scrollVerticallyBy(dy: Int,
                                    recycler: RecyclerView.Recycler?,
                                    state: RecyclerView.State?): Int {
        var newDy = dy

        //скроллим сверху вниз, новые элементы будут сверху
        if (newDy < 0 && yOffset - newDy > 0) {
            newDy = yOffset
        }

        //скроллим снизу вверх, новые элементы будут снизу
        if (newDy > 0 && tableHeight < height) {
            newDy = 0 //таблица меньше высоты экрана
        }
        else if (newDy > 0 && yOffset + tableHeight - newDy < height) {
            newDy = yOffset + tableHeight - height
        }

        if (newDy != 0) {
            offsetChildrenVertical(-newDy)
            yOffset += -newDy
            recycler?.let { processingAllItems(it) }
        }
        return newDy
    }

    /**
     * Отображение и положение каждого элемента на экране.
     * @param recycler поле, в котором размещены элементы.
     * @param state состояние поля, в котором размещены элементы.
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (recycler == null || state == null || width == 0 || height == 0) {
            return
        }

        if (state.itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        processingAllItems(recycler)
    }

    /**
     * Выполнить сброс всех параметров
     */
    fun initParams() {
        widthColumns = mutableMapOf()
        heightRows = mutableMapOf()
        bindRowColumnCells = Rect()
        xOffset = 0
        yOffset = 0
        tableWidth = 0
        tableHeight = 0
        resizeAllVisibleCells()
    }

    /**
     * Основной метод, в котором добавляются, увеличиваются, смещаются и удаляются ячейки таблицы.
     * @param recycler поле, в котором размещены элементы.
     */
    private fun processingAllItems(recycler: RecyclerView.Recycler) {
        val isFirst = childCount == 0
        if (isFirst) {
            fillFirstCells(recycler)
            resizeAllVisibleCells()
        }

        //удаляем ячейки, которые вышли за пределы
        deleteInvisibleCells(recycler)
        if (isFirst) {
            return
        }

        var isAddedNew: Boolean
        do {
            //вычисляем все близлежащие ячейки, которые вскоре должны появится на экране
            val nextVisible: Set<Pair<Int, Int>> = getNextVisibleCell()

            //добавляем новые ячейки в recyclerView и делаем по ним изменение размеров
            isAddedNew = nextVisible.isNotEmpty()
            if (isAddedNew) {
                nextVisible.forEach { item ->
                    val position = cellToPosition(item)
                    if (position < itemCount) {
                        addViewCell(recycler, position, item)
                    }
                }
                resizeAllVisibleCells()
            }
        } while (isAddedNew)

        addViewsIfNotPresent(recycler)
    }

    /**
     * Самое первое заполнение ячеек в recyclerView. Количество столбцов, до которого происходит
     * заполнение определяется первым столбцом, правый край которого оказывается справа
     * за пределами экрана в первой строке.
     * А количество строк определяется строкой, нижний край которой оказывается снизу
     * за пределами экрана.
     * @param recycler поле, в котором размещены элементы.
     */
    private fun fillFirstCells(recycler: RecyclerView.Recycler) {
        var lastRow = 0
        var lastColumn = 0
        for (position in 0 until itemCount) {
            val cell = RecyclerTableViewUtils.positionToCell(position, countColumns)

            if (((lastRow > 0 && cell.first > lastRow) || (lastColumn > 0 && cell.second > lastColumn))) {
                continue
            }

            //вычисляем позицию для новой ячейки и добавляем ее на экран
            val newRegion: NewRegion = calculateNewRegion(cell)
            val oldRegion = addViewCell(recycler, position, cell, newRegion)

            if (lastColumn == 0 && oldRegion.right > width) {
                lastColumn = cell.second
            }

            if (lastRow == 0 && oldRegion.bottom > height) {
                lastRow = cell.first
            }
        }
    }

    /**
     * Получить положение для новой ячейки на экране.
     * @param newCell ячейка, которая добавляется на экран.
     * @return позиция на экране для новой добавляемой ячейки, в которой заполнено либо
     * левая и верхняя координаты, либо правая и нижняя.
     */
    private fun calculateNewRegion(newCell: Pair<Int, Int>): NewRegion
    {
        if (newCell.first == 0 && newCell.second == 0) {
            return NewRegion(Pair(0, 0))
        }

        //вычисляем все соседние элементы
        val newOnLeft: Pair<Int, Int>? = getRightForCell(newCell)
        val newOnUp: Pair<Int, Int>? = getBottomForCell(newCell)
        val newOnRight: Pair<Int, Int>? = getLeftForCell(newCell)
        val newOnBottom: Pair<Int, Int>? = getUpForCell(newCell)

        var leftTop: Pair<Int, Int>? = null
        var rightBottom: Pair<Int, Int>? = null
        var leftRegion: Int? = null
        var topRegion: Int? = null
        var rightRegion: Int? = null
        var bottomRegion: Int? = null

        loopOnAllChild { _, _, viewRegion, _, cellFined ->
            if (cellFined == newOnRight) {
                leftRegion = viewRegion.right
                topRegion = viewRegion.top
            }

            if (cellFined == newOnUp) {
                rightRegion = viewRegion.right
                bottomRegion = viewRegion.top
            }

            if (cellFined == newOnLeft) {
                rightRegion = viewRegion.left
                bottomRegion = viewRegion.bottom
            }

            if (cellFined == newOnBottom) {
                leftRegion = viewRegion.left
                topRegion = viewRegion.bottom
            }

            leftRegion?.let { left ->
                topRegion?.let { top ->
                    leftTop = Pair(left, top)
                }
            }

            rightRegion?.let { right ->
                bottomRegion?.let { bottom ->
                    rightBottom = Pair(right, bottom)
                }
            }

            if (leftRegion != null || rightRegion != null) {
                return@loopOnAllChild
            }
        }

        return NewRegion(leftTop, rightBottom)
    }

    /**
     * Пересчитать и обновить размеры и положение у всех видимых ячеек на экране.
     */
    private fun resizeAllVisibleCells() {
        //сперва собираем все ячейки, которые нужно обновить
        val newSizeCells: Map<Int, Rect> = calculateSizeCells()

        //а затем обновляем их на экране
        updateSizeCells(newSizeCells)
    }

    /**
     * Пересчитать размеры и положение ячеек, которые нужно обновить на экране.
     * @return мапа. В ключе порядковый номер элемента в recyclerView,
     * в значении - новое положение элемента на экране.
     */
    private fun calculateSizeCells(): Map<Int, Rect> {
        val newSizeCells: MutableMap<Int, Rect> = mutableMapOf()

        loopOnAllChild { _, viewPosition, viewRegion, _, cell ->
            //ширина всех предыдущих колонок с учетом смещения
            var prevMaxWidth = xOffset
            for (ind in 0 until cell.second) {
                prevMaxWidth += widthColumns[ind] ?: 0
            }
            //высота всех предыдущих строк
            var prevMaxHeight = yOffset
            for (ind in 0 until cell.first) {
                prevMaxHeight += heightRows[ind] ?: 0
            }

            val maxWidth = widthColumns[cell.second] ?: 0
            val maxHeight = heightRows[cell.first] ?: 0
            val correctRegion = Rect(
                prevMaxWidth,
                prevMaxHeight,
                prevMaxWidth + maxWidth,
                prevMaxHeight + maxHeight)

            if (correctRegion != viewRegion) {
                //увеличиваем размеры
                newSizeCells[viewPosition] = correctRegion
            }
        }

        return newSizeCells
    }

    /**
     * Обновить размеры и положение у всех видимых ячеек на экране.
     * @param sizeCells размеры и положение ячеек для обновления.
     */
    private fun updateSizeCells(sizeCells: Map<Int, Rect>) {
        sizeCells.forEach { item ->
            val view = getChildAt(item.key)
            view?.let {
                val viewWidth = item.value.right - item.value.left
                val viewHeight = item.value.bottom - item.value.top
                it.layoutParams.width = viewWidth
                it.layoutParams.height = viewHeight

                layoutDecoratedWithMargins(
                    it,
                    item.value.left,
                    item.value.top,
                    item.value.right,
                    item.value.bottom)
            }
        }
    }

    /**
     * Удалить ячейки, с recyclerView, которые вышли за пределы [numberPreloadedItems].
     * @param recycler поле, в котором размещены элементы.
     */
    private fun deleteInvisibleCells(recycler: RecyclerView.Recycler) {
        val viewDeleted: MutableMap<Pair<Int, Int>, View?> = mutableMapOf()

        //определяем видимые слева и справа столбцы таблицы
        val leftRight: Pair<Int, Int> = getVisibleColumns()
        val visibleLeft = leftRight.first
        val visibleRight = leftRight.second

        //определяем видимые сверху и снизу строки таблицы
        val topBottom: Pair<Int, Int> = getVisibleRows()
        val visibleTop = topBottom.first
        val visibleBottom = topBottom.second

        //определяем столбцы, слева включительно до которых и справа включительно после которых,
        // можно удалять ячейки
        val deletedLeft: Int? =
            (visibleLeft - numberPreloadedItems -1).takeIf { it >= bindRowColumnCells.left }
        val deletedRight: Int? =
            (visibleRight + numberPreloadedItems +1).takeIf { it <= bindRowColumnCells.right }

        //определяем строки, сверху включительно до которых и снизу включительно после которых,
        // можно удалять ячейки
        val deletedTop: Int? =
            (visibleTop - numberPreloadedItems -1).takeIf { it >= bindRowColumnCells.top }
        val deletedBottom: Int? =
            (visibleBottom + numberPreloadedItems +1).takeIf { it <= bindRowColumnCells.bottom }

        //удаляемые ячейки слева
        var newBindLeft: Int = bindRowColumnCells.left
        deletedLeft?.let {
            getBindingCellToLeft(it).forEach { cell -> viewDeleted[cell] = null }
            newBindLeft = it +1
        }
        //удаляемые ячейки справа
        var newBindRight: Int = bindRowColumnCells.right
        deletedRight?.let {
            getBindingCellToRight(it).forEach { cell -> viewDeleted[cell] = null }
            newBindRight = it -1
        }
        //удаляемые ячейки сверху
        var newBindTop: Int = bindRowColumnCells.top
        deletedTop?.let {
            getBindingCellToTop(it).forEach { cell -> viewDeleted[cell] = null }
            newBindTop = it +1
        }
        //удаляемые ячейки снизу
        var newBindBottom: Int = bindRowColumnCells.bottom
        deletedBottom?.let {
            getBindingCellToBottom(it).forEach { cell -> viewDeleted[cell] = null }
            newBindBottom = it -1
        }

        //находим view для удаления
        setLinkViewWithCell(viewDeleted)

        //удаляем view ячеек с экрана
        if (viewDeleted.isNotEmpty()) {
            viewDeleted
                .keys.forEach { cell ->
                    viewDeleted[cell]?.let {
                        //сбрасываем размеры, чтобы при повторном использовании они были актуальными
                        it.layoutParams.height = LayoutParams.WRAP_CONTENT
                        it.layoutParams.width = LayoutParams.WRAP_CONTENT
                        detachAndScrapView(it, recycler)
                    }
            }
        }

        bindRowColumnCells.left = newBindLeft
        bindRowColumnCells.right = newBindRight
        bindRowColumnCells.top = newBindTop
        bindRowColumnCells.bottom = newBindBottom
    }

    /**
     * Связать ячейки таблицы с конкретными view на экране.
     * @param cells мапа. В ключе записаны ячейки в таблицы, которые можно удалять,
     * а в значении будет записана соответствующая view.
     * @return обновленная мапа [cells]
     */
    private fun setLinkViewWithCell(cells: MutableMap<Pair<Int, Int>, View?>) {
        if (cells.isEmpty()) {
            return
        }

        loopOnAllChild { view, _, _, _, cell ->
            val isDeleted = cells.containsKey(cell)
                    || (fixHeader && cell.first == 0)
                    || (fixColumn && cell.second == 0)
            //пропускаем закрепленные ячейки, их не нужно удалять
            val isVisibleFixedHeader = cellIsFixHeader(cell) && columnInHorizontal(cell.second)
            val isVisibleFixedColumn = cellIsFixColumn(cell) && rowInVertical(cell.first)
            if (!isVisibleFixedHeader && !isVisibleFixedColumn && isDeleted) {
                cells[cell] = view
            }
        }
    }

    /**
     * Вычислить все близлежащие ячейки, которые нужно добавить в recyclerView.
     * @return Список координат ячеек в таблице.
     */
    private fun getNextVisibleCell(): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()
        //сперва собираем все возможные ячейки, которые могут быть
        val allCalculateCells: Map<Pair<Int, Int>, List<Pair<Int, Int>>> = getCalculateCell()

        //обходим периметр ячеек, которые уже добавлены в RecyclerView
        val addedRows: MutableSet<Int> = mutableSetOf()
        val addedColumns: MutableSet<Int> = mutableSetOf()
        for (row in bindRowColumnCells.top .. bindRowColumnCells.bottom) {
            for (column in bindRowColumnCells.left .. bindRowColumnCells.right) {
                //обрабатываем ячейки, у которых есть новые ячейки по соседству
                val bindingCell = Pair(row, column)
                val calculateCells = allCalculateCells[bindingCell] ?: continue
                calculateCells.forEach { result.add(it) }
                if (fixColumn) {
                    addedRows.add(row)
                }
                if (fixHeader) {
                    addedColumns.add(column)
                }
            }
        }

        //добавляем фиксированные ячейки
        if (result.isNotEmpty()) {
            addedRows.forEach {
                val fixCell = Pair(it, 0)
                result.add(fixCell)
            }
            addedColumns.forEach {
                val fixCell = Pair(0, it)
                result.add(fixCell)
            }
        }

        return result
    }

    /**
     * Получить список всех соседних ячеек, которые рядом с видимыми ячейками.
     * @return мапа. В ключе лежит добавленная ячейка в recyclerView,
     * в значении - список соседних ячеек, которые возможно нужно обработать.
     */
    private fun getCalculateCell(): Map<Pair<Int, Int>, List<Pair<Int, Int>>> {
        //вычисляем возможно новые колонки слева и справа
        val leftRight: Pair<Int, Int> = getVisibleColumns()
        val bindLeft = leftRight.first - numberPreloadedItems
        val bindRight = leftRight.second + numberPreloadedItems
        //вычисляем возможно новые строки сверху и снизу
        val topBottom: Pair<Int, Int> = getVisibleRows()
        val bindTop = topBottom.first - numberPreloadedItems
        val bindBottom = topBottom.second + numberPreloadedItems

        val result: MutableMap<Pair<Int, Int>, MutableList<Pair<Int, Int>>> = mutableMapOf()
        //слева
        for (column in bindLeft until bindRowColumnCells.left) {
            if (!isColumnInTable(column)) {
                continue
            }
            for (row in bindTop .. bindBottom) {
                if (!isRowInTable(row)) {
                    continue
                }
                val bindRow = when {
                    row < bindRowColumnCells.top -> bindRowColumnCells.top
                    row > bindRowColumnCells.bottom -> bindRowColumnCells.bottom
                    else -> row
                }
                val bindItem = Pair(bindRow, bindRowColumnCells.left)
                val list = result[bindItem] ?: mutableListOf()
                list.add(Pair(row, column))
                result[bindItem] = list
            }
        }

        //сверху
        for (row in bindTop until bindRowColumnCells.top) {
            if (isRowInTable(row)) {
                for (column in bindRowColumnCells.left .. bindRowColumnCells.right) {
                    val bindItem = Pair(bindRowColumnCells.top, column)
                    val list = result[bindItem] ?: mutableListOf()
                    list.add(Pair(row, column))
                    result[bindItem] = list
                }
            }
        }

        //справа
        for (column in bindRowColumnCells.right +1 .. bindRight) {
            if (!isColumnInTable(column)) {
                continue
            }
            for (row in bindTop .. bindBottom) {
                if (!isRowInTable(row)) {
                    continue
                }
                val bindRow = when {
                    row < bindRowColumnCells.top -> bindRowColumnCells.top
                    row > bindRowColumnCells.bottom -> bindRowColumnCells.bottom
                    else -> row
                }
                val bindItem = Pair(bindRow, bindRowColumnCells.right)
                val list = result[bindItem] ?: mutableListOf()
                list.add(Pair(row, column))
                result[bindItem] = list
            }
        }

        //снизу
        for (row in bindRowColumnCells.bottom +1 .. bindBottom) {
            if (isRowInTable(row)) {
                for (column in bindRowColumnCells.left .. bindRowColumnCells.right) {
                    val bindItem = Pair(bindRowColumnCells.bottom, column)
                    val list = result[bindItem] ?: mutableListOf()
                    list.add(Pair(row, column))
                    result[bindItem] = list
                }
            }
        }

        return result
    }

    /**
     * Вычислить видимые столбцы таблицы слева и справа экрана.
     * Ячейки, которые одной стороной еще/уже видны.
     * @return пара. На первом месте видимый столбец слева, на втором - видимый столбец справа.
     */
    private fun getVisibleColumns(): Pair<Int, Int> {
        var visibleLeft = bindRowColumnCells.left
        var visibleRight = bindRowColumnCells.right
        var sumWidth = 0
        var leftReceived = false
        var rightReceived = false
        for (column in 0 until countColumns) {
            val widthColumn = widthColumns[column] ?: 0
            if (abs(xOffset) in sumWidth until sumWidth + widthColumn) {
                visibleLeft = column
                leftReceived = true
            }

            if (width + abs(xOffset) in sumWidth until sumWidth + widthColumn) {
                visibleRight = column
                rightReceived = true
            }

            if (leftReceived && rightReceived) {
                break
            }

            sumWidth += widthColumn
        }

        return Pair(visibleLeft, visibleRight)
    }

    /**
     * Вычислить видимые строки таблицы сверху и снизу экрана.
     * Ячейки, которые одной стороной еще/уже видны.
     * @return пара. На первом месте видимая строка сверху, на втором - видимая строка снизу.
     */
    private fun getVisibleRows(): Pair<Int, Int> {
        var visibleTop = bindRowColumnCells.top
        var visibleBottom = bindRowColumnCells.bottom
        var sumHeight = 0
        var topReceived = false
        var bottomReceived = false
        for (row in 0 .. RecyclerTableViewUtils.positionToCell(itemCount, countColumns).first) {
            val heightRow = heightRows[row] ?: 0
            if (abs(yOffset) in sumHeight until sumHeight + heightRow) {
                visibleTop = row
                topReceived = true
            }

            if (height + abs(yOffset) in sumHeight until sumHeight + heightRow) {
                visibleBottom = row
                bottomReceived = true
            }

            if (topReceived && bottomReceived) {
                break
            }

            sumHeight += heightRow
        }

        return Pair(visibleTop, visibleBottom)
    }

    /**
     * Добавить конкретную ячейку в recyclerView.
     * @param recycler поле, в котором размещены элементы.
     * @param adapterPosition позиция ячейки в адаптере.
     * @param cell координаты ячейки в таблице.
     * @param newRegion позиция на экране для добавления в recyclerView.
     * @return координаты добавленной ячейки на экране.
     */
    private fun addViewCell(recycler: RecyclerView.Recycler,
                            adapterPosition: Int,
                            cell: Pair<Int, Int>,
                            newRegion: NewRegion? = null): Rect
    {
        val view = recycler.getViewForPosition(adapterPosition)
        addView(view)

        measureChildWithMargins(view, 0, 0)
        updateMaxWidthHeight(view, cell)

        //вычисляем положение для элемента на экране
        val cellWidth = widthColumns[cell.second] ?: throw IncorrectViewSize("Некорректная ширина новой ячейки")
        val cellHeight = heightRows[cell.first] ?: throw IncorrectViewSize("Некорректная высота новой ячейки")
        val cellRegion = newRegion?.let {
            val region = calculateCellRegion(it, cellWidth, cellHeight)
            layoutDecoratedWithMargins(view, region.left, region.top, region.right, region.bottom)
            region
        }

        //обновим данные по добавленным ячейкам в RecyclerView
        if (cell.first < bindRowColumnCells.top) {
            bindRowColumnCells.top = cell.first
        }
        if (cell.first > bindRowColumnCells.bottom) {
            bindRowColumnCells.bottom = cell.first
        }
        if (cell.second < bindRowColumnCells.left) {
            bindRowColumnCells.left = cell.second
        }
        if (cell.second > bindRowColumnCells.right) {
            bindRowColumnCells.right = cell.second
        }

        return  cellRegion ?: Rect()
    }

    /**
     * Получить список добавленных ячеек в recyclerView, которые слева от [left] включительно.
     * @param left столбец, до которого слева нужно получить ячейки включительно.
     * @return сет ячеек, расположенных левее по всем строкам.
     */
    private fun getBindingCellToLeft(left: Int): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()

        for (row in bindRowColumnCells.top .. bindRowColumnCells.bottom) {
            for (column in bindRowColumnCells.left .. left) {
                result.add(Pair(row, column))
            }
        }

        return result
    }

    /**
     * Получить список добавленных ячеек в recyclerView, которые справа от [right] включительно.
     * @param right столбец, после которого справа нужно получить ячейки включительно.
     * @return сет ячеек, расположенных правее по всем строкам.
     */
    private fun getBindingCellToRight(right: Int): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()

        for (row in bindRowColumnCells.top .. bindRowColumnCells.bottom) {
            for (column in right .. bindRowColumnCells.right) {
                result.add(Pair(row, column))
            }
        }

        return result
    }

    /**
     * Получить список добавленных ячеек в recyclerView, которые выше от [top] включительно.
     * @param top строка, до которой сверху нужно получить ячейки включительно.
     * @return сет ячеек, расположенных выше по всем столбцам.
     */
    private fun getBindingCellToTop(top: Int): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()

        for (row in bindRowColumnCells.top .. top) {
            for (column in bindRowColumnCells.left .. bindRowColumnCells.right) {
                result.add(Pair(row, column))
            }
        }

        return result
    }

    /**
     * Получить список добавленных ячеек в recyclerView, которые ниже от [bottom] включительно.
     * @param bottom строка, после которой ниже нужно получить ячейки включительно.
     * @return сет ячеек, расположенных ниже по всем столбцам.
     */
    private fun getBindingCellToBottom(bottom: Int): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()

        for (row in bottom .. bindRowColumnCells.bottom) {
            for (column in bindRowColumnCells.left .. bindRowColumnCells.right) {
                result.add(Pair(row, column))
            }
        }

        return result
    }

    /**
     * Рассчитать положение на экране для новой ячейки на основе существующей ячейки
     * с координатами [newRegion], шириной [cellWidth] и высотой [cellHeight] новой ячейки.
     * @param newRegion координаты соседней ячейки, около которой нужно добавить новую.
     * @param cellWidth ширина новой добавляемой ячейки.
     * @param cellHeight высота новой добавляемой ячейки.
     */
    private fun calculateCellRegion(newRegion: NewRegion, cellWidth: Int, cellHeight: Int): Rect
    {
        val leftTop = newRegion.leftTop
        val rightBottom = newRegion.rightBottom
        return when {
            leftTop != null -> Rect(
                leftTop.first,
                leftTop.second,
                leftTop.first + cellWidth,
                leftTop.second + cellHeight)
            rightBottom != null -> Rect(
                rightBottom.first - cellWidth,
                rightBottom.second - cellHeight,
                rightBottom.first,
                rightBottom.second)
            else -> Rect()
        }
    }

    /**
     * Обновить максимальную высоту в строке и максимальную ширину в столбце.
     * @param view элемент на экране, у которого возможно самые большие размеры.
     * @param cell координаты ячейки в таблице.
     */
    private fun updateMaxWidthHeight(view: View, cell: Pair<Int, Int>) {
        val viewWidth = getDecoratedMeasuredWidth(view)
        val viewHeight = getDecoratedMeasuredHeight(view)

        val oldWidth = widthColumns[cell.second] ?: 0
        if (viewWidth > oldWidth) {
            widthColumns[cell.second] = viewWidth
            tableWidth = widthColumns.values.sum()
        }
        else if (viewWidth < oldWidth) {
            view.layoutParams.width = oldWidth
        }

        val oldHeight = heightRows[cell.first] ?: 0
        if (viewHeight > oldHeight) {
            heightRows[cell.first] = viewHeight
            tableHeight = heightRows.values.sum()
        }
        else if (viewHeight < oldHeight) {
            view.layoutParams.height = oldHeight
        }
    }

    /**
     * Получить координаты левой ячейки в таблице.
     * @param cell ячейка, от которой нужно получить координаты левой ячейки таблицы.
     * @return координаты ячейки или null, если слева не может быть ячеек.
     */
    private fun getLeftForCell(cell: Pair<Int, Int>): Pair<Int, Int>? =
        if (cell.second -1 < 0)
            null
        else
            Pair(cell.first, cell.second -1)

    /**
     * Получить координаты верхней ячейки в таблице.
     * @param cell ячейка, от которой нужно получить координаты верхней ячейки таблицы.
     * @return координаты ячейки или null, если сверху не может быть ячеек.
     */
    private fun getUpForCell(cell: Pair<Int, Int>): Pair<Int, Int>? =
        if (cell.first -1 < 0)
            null
        else
            Pair(cell.first -1, cell.second)

    /**
     * Получить координаты правой ячейки в таблице.
     * @param cell ячейка, от которой нужно получить координаты правой ячейки таблицы.
     * @return координаты ячейки или null, если справа не может быть ячеек.
     */
    private fun getRightForCell(cell: Pair<Int, Int>): Pair<Int, Int>? =
        if (cell.second +1 > countColumns -1)
            null
        else
            Pair(cell.first, cell.second +1)

    /**
     * Получить координаты нижней ячейки в таблице.
     * @param cell ячейка, от которой нужно получить координаты нижней ячейки таблицы.
     * @return координаты ячейки или null, если снизу не может быть ячеек.
     */
    private fun getBottomForCell(cell: Pair<Int, Int>): Pair<Int, Int>? {
        val newCell = Pair(cell.first + 1, cell.second)
        return if (cellToPosition(newCell) > itemCount)
            null
        else
            newCell
    }

    /**
     * Проверить не выходит ли номер строки за пределы таблицы.
     * @return true - номер строки корректный, за пределы таблицы не выходит,
     * false - номер строки выходит за пределы таблицы.
     */
    private fun isRowInTable(row: Int): Boolean =
        row >= 0 && row <= RecyclerTableViewUtils.positionToCell(itemCount-1, countColumns).first

    /**
     * Проверить не выходит ли номер столбца за пределы таблицы.
     * @return true - номер столбца корректный, за пределы таблицы не выходит,
     * false - номер столбца выходит за пределы таблицы.
     */
    private fun isColumnInTable(column: Int): Boolean = column in 0 until countColumns

    /**
     * Обойти все видимые элементы на экране и с каждым сделать какое-то действие [event].
     * @param event действие, которое нужно сделать с каждым элементом на экране.
     * Содержит в себе:
     * + __view__ - сам элемент.
     * + __viewPosition__ - порядковый номер элемента на экране (как по счету в recyclerView).
     * + __viewRegion__ - позиция элемента на экране.
     * + __position__ - позиция элемента в адаптере.
     * + __cell__ - координаты элемента в таблице.
     */
    private fun loopOnAllChild(event: (view: View,
                                       viewPosition: Int,
                                       viewRegion: Rect,
                                       position: Int,
                                       cell: Pair<Int, Int>) -> Unit) {
        for (ind in 0 until childCount) {
            val view = getChildAt(ind)
            view?.let {
                val position = getPosition(it)
                val cell = RecyclerTableViewUtils.positionToCell(position, countColumns)
                val viewRegion: Rect = getViewRegion(it)
                event(it, ind, viewRegion, position, cell)
            }
        }
    }

    /**
     * Добавить недостающие view.
     * @param recycler поле, в котором размещены элементы.
     */

    private fun addViewsIfNotPresent(recycler: RecyclerView.Recycler) {
        val cellsChild = getCellsChild()
        val cellsVisible = getCellsVisible()

        val notExists: Set<Pair<Int, Int>> = cellsVisible.minus(cellsChild)
        if (notExists.isNotEmpty()) {
            notExists.forEach { item ->
                val position = cellToPosition(item)
                if (position < itemCount) {
                    addViewCell(recycler, position, item)
                }
            }
            resizeAllVisibleCells()
        }
    }

    /**
     * Получить координаты ячеек, view которых есть на экране.
     */
    private fun getCellsChild(): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()
        for (ind in 0 until childCount) {
            val view = getChildAt(ind)
            view?.let {
                val position = getPosition(it)
                val cell = RecyclerTableViewUtils.positionToCell(position, countColumns)
                result.add(cell)
            }
        }
        return result
    }

    /**
     * Получить координаты видимых ячеек.
     */
    private fun getCellsVisible(): Set<Pair<Int, Int>> {
        val result: MutableSet<Pair<Int, Int>> = mutableSetOf()
        //определяем видимые сверху, снизу строки и слева, справа столбцы таблицы
        val topBottom: Pair<Int, Int> = getVisibleRows()
        val leftRight: Pair<Int, Int> = getVisibleColumns()
        for (row in topBottom.first .. topBottom.second) {
            for (column in leftRight.first .. leftRight.second) {
                result.add(Pair(row, column))

                //столбцы в закрепленной строке
                if (topBottom.first > 0 && fixHeader) {
                    result.add(Pair(0, column))
                }
            }

            //строки в закрепленном столбцы
            if (leftRight.first > 0 && fixColumn) {
                result.add(Pair(row, 0))
            }
        }

        //левый верхний угол
        if (fixHeader || fixColumn) {
            result.add(Pair(0, 0))
        }

        return result
    }

    /**
     * Получить координаты на экране у конкретного элемента.
     * @param view элемент, у которого нужно получить координаты.
     * @return координаты положения элемента на экране.
     */
    private fun getViewRegion(view: View): Rect
    {
        val result = Rect()
        result.left = getDecoratedLeft(view)
        result.top = getDecoratedTop(view)
        result.right = getDecoratedRight(view)
        result.bottom = getDecoratedBottom(view)
        return result
    }

    /**
     * Преобразовать координаты элемента в таблице к положению элемента в адаптере.
     * @param cell пара, содержащая номер строки и столбца ячейки в таблице.
     * @return позиция элемента в адаптере.
     */
    private fun cellToPosition(cell: Pair<Int, Int>): Int = cell.first * countColumns + cell.second

    /**
     * Проверить закреплена ли ячейка в шапке таблицы.
     * @param cell пара, содержащая номер строки и столбца ячейки в таблице.
     * @return True - ячейка закреплена в шапке таблице, false - не закреплена.
     */
    private fun cellIsFixHeader(cell: Pair<Int, Int>): Boolean = fixHeader && cell.first == 0

    /**
     * Проверить закреплена ли ячейка в левом столбце таблицы.
     * @param cell пара, содержащая номер строки и столбца ячейки в таблице.
     * @return True - ячейка закреплена в левом столбце таблице, false - не закреплена.
     */
    private fun cellIsFixColumn(cell: Pair<Int, Int>): Boolean = fixColumn && cell.second == 0

    /**
     * Проверить, входит ли столбец в список столбцов, добавленных на экран.
     * @param column Столбец для проверки.
     * @return True - столбец входит в список добавленных столбцов на экране, false - не входит.
     */
    private fun columnInHorizontal(column: Int): Boolean =
        column in bindRowColumnCells.left .. bindRowColumnCells.right

    /**
     * Проверить, входит ли строка в список строк, добавленных на экран.
     * @param row Строка для проверки.
     * @return True - строка входит в список добавленных строк на экране, false - не входит.
     */
    private fun rowInVertical(row: Int): Boolean =
        row in bindRowColumnCells.top .. bindRowColumnCells.bottom
}