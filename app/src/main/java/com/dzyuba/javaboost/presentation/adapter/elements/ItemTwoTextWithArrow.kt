package com.dzyuba.javaboost.presentation.adapter.elements

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.presentation.adapter.BaseItem
import com.dzyuba.javaboost.databinding.ItemTwoTextWithArrowBinding
import com.dzyuba.javaboost.util.setVisibleOrGone

class ItemTwoTextWithArrow(id: Int = -1, item: Any? = null) : BaseItem(id, Types.TextWithArrow, item) {

    var leftText = ""
    var rightText = ""
    var binding: ItemTwoTextWithArrowBinding? = null
    var positionType: PositionType = PositionType.SOLO
    var imageRes: Int = R.drawable.ic_arrow_right
    var onItemClick: (() -> Unit)? = null

    override fun getView(): View? = binding?.root

    override fun createView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        binding = ItemTwoTextWithArrowBinding.inflate(layoutInflater, viewGroup, false)
        refresh()
        return binding?.root!!
    }

    fun setVisible(visible: Boolean) {
        binding?.root?.setVisibleOrGone(visible)
    }

    private fun getBackgroundId(): Int {
        return when (positionType) {
            PositionType.TOP -> R.drawable.bg_item_top

            PositionType.MIDDLE -> R.drawable.bg_item_middle

            PositionType.BOTTOM -> R.drawable.bg_item_bottom

            PositionType.SOLO -> R.drawable.bg_item_solo
        }
    }

    override fun refresh() {
        binding?.leftText?.text = leftText
        binding?.rightText?.text = rightText
        binding?.leftText?.setOnClickListener {
            onItemClick?.invoke()
        }
        binding?.root?.setOnClickListener {
            onItemClick?.invoke()
        }
        binding?.icon?.apply {
            setImageResource(imageRes)
//            if (onItemClick == null)
//                isClickable = false
        }
        binding?.root?.setBackgroundResource(getBackgroundId())
    }

    enum class PositionType {
        TOP,
        MIDDLE,
        BOTTOM,
        SOLO
    }
}