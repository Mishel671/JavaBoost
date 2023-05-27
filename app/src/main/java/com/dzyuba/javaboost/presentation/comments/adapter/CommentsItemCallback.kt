package com.dzyuba.javaboost.presentation.comments.adapter

import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.dzyuba.javaboost.domain.entities.lesson.Comment

object CommentsItemCallback : ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }


}