package com.github.ekiryushin.recyclertableview.example.ui.dynamicdata

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.github.ekiryushin.recyclertableview.example.data.TableSource

class DynamicPresenter {

    /**
     * Получить liveData данных для таблицы
     */
    fun getLiveData(): LiveData<PagingData<String>> {
        val posts = { DynamicDataSource(TableSource()) }

        return Pager(
            config = getDefaultPageConfig(),
            pagingSourceFactory = posts
        ).liveData
    }

    /**
     * Получить настройки для постраничного отображения категорий
     */
    private fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(
            pageSize = DynamicDataSource.COUNT_ROW_IN_PAGE * DynamicDataSource.COUNT_COLUMN,
            enablePlaceholders = false)
    }
}