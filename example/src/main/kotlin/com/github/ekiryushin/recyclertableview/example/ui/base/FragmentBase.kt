package com.github.ekiryushin.recyclertableview.example.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.ekiryushin.recyclertableview.example.databinding.TableFragmentBinding

abstract class FragmentBase: Fragment() {

    lateinit var binding: TableFragmentBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = TableFragmentBinding.inflate(inflater, container, false)

        initView()

        binding.fab.setOnClickListener { recreateData() }

        return binding.root
    }

    abstract fun initView()

    abstract fun recreateData()
}