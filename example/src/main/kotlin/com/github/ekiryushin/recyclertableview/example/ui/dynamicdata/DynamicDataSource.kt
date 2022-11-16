package com.github.ekiryushin.recyclertableview.example.ui.dynamicdata

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.ekiryushin.recyclertableview.core.LogLevel
import com.github.ekiryushin.recyclertableview.core.RecyclerTableViewUtils
import com.github.ekiryushin.recyclertableview.example.data.TableSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Формирование постраничных даных для таблицы
 */
class DynamicDataSource(private val source: TableSource): PagingSource<Int, String>() {

    companion object {
        /** Количество строк таблицы на каждой странице */
        const val COUNT_ROW_IN_PAGE = 5

        /** Количество столбцов в таблице */
        const val COUNT_COLUMN = 21
    }

    /**
     * Загрузить данные для таблицы в фоновом процессе.
     * @params параметры для очередной загрузки данных.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return withContext(Dispatchers.IO) {
            try {
                val page: Int = params.key ?: 0
                val nextPage: Int = page + 1
                val prevPage: Int? = if (page == 0) null else page - 1

                RecyclerTableViewUtils.toLog(LogLevel.DEBUG, "-------------------------")
                RecyclerTableViewUtils.toLog(LogLevel.DEBUG, "Начало формирования данных для page=$page")

                val dataTable: List<List<String>> = generateData(page)
                val data: List<String> = dataTable.flatten()
                RecyclerTableViewUtils.toLog(LogLevel.DEBUG, "Окончание формирования данных для page=$page")

                LoadResult.Page(
                    data = data,
                    prevKey = prevPage,
                    nextKey = nextPage
                )
            }
            catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }

    /**
     * Сформировать данные для таблицы по конкретной странице.
     * @param page номер страницы с данными.
     * @return список строк в таблице, в каждой строке находится список значений для столбцов таблицы.
     */
    private suspend fun generateData(page: Int): List<List<String>> {
        //имитируем долгую загрузку
        if (page > 0) {
            delay(500)
        }
        return source.getData(page * COUNT_ROW_IN_PAGE, COUNT_ROW_IN_PAGE, COUNT_COLUMN)
    }

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        val anchorPosition: Int = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}