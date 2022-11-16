package com.github.ekiryushin.recyclertableview.example.ui.activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainViewPageAdapter(fragmentActivity: FragmentActivity,
                          private val fragments: List<Pair<Fragment, String>>): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position].first
}