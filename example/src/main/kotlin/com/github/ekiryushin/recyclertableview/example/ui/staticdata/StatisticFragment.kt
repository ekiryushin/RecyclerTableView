package com.github.ekiryushin.recyclertableview.example.ui.staticdata

import androidx.recyclerview.widget.DefaultItemAnimator
import com.github.ekiryushin.recyclertableview.TableLayoutManager
import com.github.ekiryushin.recyclertableview.example.data.TableSource
import com.github.ekiryushin.recyclertableview.example.ui.base.FragmentBase

/**
 * Экран с полностью сформированными данными для таблицы.
 */
class StatisticFragment: FragmentBase() {

    private val presenter = StatisticPresenter(TableSource())

    /**
     * Инициализация view на экране.
     */
    override fun initView() {
        val data: List<List<String>> = presenter.getData()
        val dataAdapter = StatisticAdapter(data)
        val countColumns = dataAdapter.countColumns

        with(binding) {
            recyclerView.adapter = dataAdapter
            recyclerView.layoutManager = TableLayoutManager(
                requireContext(),
                countColumns,
                fixHeader = true,
                fixColumn = true)
            recyclerView.itemAnimator = DefaultItemAnimator()
        }
    }

    override fun recreateData() {
        initView()
    }
}