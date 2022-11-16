package com.github.ekiryushin.recyclertableview.example.ui.dynamicdata

import androidx.lifecycle.lifecycleScope
import com.github.ekiryushin.recyclertableview.TableLayoutManager
import com.github.ekiryushin.recyclertableview.example.ui.base.FragmentBase
import kotlinx.coroutines.launch

/**
 * Экран с динамически загружаемыми данными
 */
class DynamicFragment: FragmentBase() {

    private val presenter = DynamicPresenter()
    private var adapterData = DynamicAdapter(DynamicDataSource.COUNT_COLUMN)

    /**
     * Инициализация view на экране.
     */
    override fun initView() {
        with(binding) {
            recyclerView.adapter = adapterData
            recyclerView.layoutManager = TableLayoutManager(
                requireContext(),
                DynamicDataSource.COUNT_COLUMN,
                fixHeader = true,
                fixColumn = true)
            recyclerView.itemAnimator = null
        }
        recreateData()
    }

    override fun recreateData() {
        binding.recyclerView.refresh()
        //подписываемся на обновления данных для таблицы
        presenter.getLiveData().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapterData.submitData(it)
            }
        }
    }
}