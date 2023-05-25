package com.dzyuba.javaboost.presentation.adapter.elements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dzyuba.javaboost.databinding.ItemDividerBigBinding
import com.dzyuba.javaboost.presentation.adapter.BaseItem

class ItemDividerBig(id : Int = -1) : BaseItem(id, Types.DividerBig) {

    var binding : ItemDividerBigBinding? = null

    override fun getView(): View? = binding?.root

    override fun createView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        binding = ItemDividerBigBinding.inflate(layoutInflater, viewGroup, false)
        return binding?.root!!
    }

    override fun refresh() {
    }
}