package com.dzyuba.javaboost.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dzyuba.javaboost.presentation.adapter.elements.Types

abstract class BaseItem(var id : Int, var type : Types, var item : Any? = null) {

    abstract fun getView() : View?

    abstract fun createView(layoutInflater: LayoutInflater, viewGroup: ViewGroup) : View

    abstract fun refresh()

    /**
     * Remove view from parent, prepare to attach to new viewGroup
     */
    open fun reset() {
        (getView()?.parent as? ViewGroup)?.removeView(getView())
    }
}