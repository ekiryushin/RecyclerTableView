package com.github.ekiryushin.recyclertableview.example.ui.activity

import androidx.fragment.app.Fragment
import com.github.ekiryushin.recyclertableview.example.ui.dynamicdata.DynamicFragment
import com.github.ekiryushin.recyclertableview.example.ui.staticdata.StatisticFragment

class MainPresenter {

    /**
     * Сформировать список фрагментов для вкладок.
     * @return сформированный список пар. На первом места фрагмент,
     * на втором - заголовок для вкладки.
     */
    fun getFragments(): List<Pair<Fragment, String>> {
        return listOf(
            Pair(StatisticFragment(), "Статичные данные"),
            Pair(DynamicFragment(), "Динамичные данные"))
    }
}