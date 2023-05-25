package com.dzyuba.javaboost.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

open class BaseAdapter(private val actionProcessor: ActionProcessor? = null) {
    var items = ArrayList<BaseItem>()

    fun getItemByID(id: Int): BaseItem? {
        return items.firstOrNull {
            it.id == id
        }
    }

    fun reset() {
        items.forEach { it.reset() }
    }

    fun installOn(viewGroup: ViewGroup, layoutInflater: LayoutInflater) {
        viewGroup.isFocusableInTouchMode = true
        items.forEach { item ->
            item.reset()
            viewGroup.addView(item.createView(layoutInflater, viewGroup))
            actionProcessor?.let {
                item.getView()?.setOnClickListener {
                    actionProcessor.onFormItemClick(item)
                }
            }
        }
    }
}