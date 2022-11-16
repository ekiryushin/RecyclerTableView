package com.github.ekiryushin.recyclertableview.example.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import io.github.ekiryushin.recyclertableview.example.databinding.MainActivityBinding

class MainActivity: AppCompatActivity() {

    private val binding: MainActivityBinding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val presenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragments: List<Pair<Fragment, String>> = presenter.getFragments()
            binding.viewpager.isUserInputEnabled = false
            binding.viewpager.adapter = MainViewPageAdapter(this, fragments)
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                tab.text = fragments[position].second
            }.attach()
        }
    }
}