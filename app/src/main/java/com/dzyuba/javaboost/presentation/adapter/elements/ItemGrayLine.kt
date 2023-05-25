package com.dzyuba.javaboost.presentation.adapter.elements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dzyuba.javaboost.databinding.ItemGrayLineBinding
import com.dzyuba.javaboost.presentation.adapter.BaseItem
import com.dzyuba.javaboost.util.setVisibleOrGone

class ItemGrayLine(id : Int = -1) : BaseItem(id, Types.GrayLine) {

    var binding : ItemGrayLineBinding? = null

    override fun getView(): View? = binding?.root

    override fun createView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        binding = ItemGrayLineBinding.inflate(layoutInflater, viewGroup, false)
        return binding?.root!!
    }

    fun setVisible(visible: Boolean) {
        binding?.root?.setVisibleOrGone(visible)
    }

    override fun refresh() {
    }
}