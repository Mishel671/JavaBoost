package com.dzyuba.javaboost.presentation.adapter.elements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.dzyuba.javaboost.databinding.ItemHeaderBinding
import com.dzyuba.javaboost.presentation.adapter.BaseItem

class ItemHeader(id: Int = -1) : BaseItem(id, Types.Header) {

    var headerText = ""

    private var binding: ItemHeaderBinding? = null

    override fun getView(): View? = binding?.root

    override fun createView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        binding = ItemHeaderBinding.inflate(layoutInflater, viewGroup, false)
        refresh()
        return binding?.root!!
    }

    override fun refresh() {
        binding?.root?.text = headerText
    }
}