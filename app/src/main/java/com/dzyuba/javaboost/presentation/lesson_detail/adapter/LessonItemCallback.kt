package com.dzyuba.javaboost.presentation.lesson_detail.adapter

import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.dzyuba.javaboost.domain.entities.lesson.*

object LessonItemCallback : ItemCallback<LessonItem>() {
    override fun areItemsTheSame(oldItem: LessonItem, newItem: LessonItem): Boolean {
        return oldItem.id == newItem.id && oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: LessonItem, newItem: LessonItem): Boolean {
        return when{
            oldItem is Header && newItem is Header && oldItem == newItem -> true
            oldItem is Text && newItem is Text && oldItem == newItem -> true
            oldItem is Divider && newItem is Divider && oldItem == newItem -> true
            oldItem is Image && newItem is Image && oldItem == newItem -> true
            oldItem is Code && newItem is Code && oldItem == newItem -> true
            oldItem is Test && newItem is Test && oldItem == newItem -> true
            oldItem is Practice && newItem is Practice && oldItem == newItem -> true
            else -> false
        }
    }

}