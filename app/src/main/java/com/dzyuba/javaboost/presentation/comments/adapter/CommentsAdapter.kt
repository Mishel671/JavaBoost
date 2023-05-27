package com.dzyuba.javaboost.presentation.comments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.dzyuba.javaboost.databinding.RvInCommentItemBinding
import com.dzyuba.javaboost.databinding.RvOutCommentItemBinding
import com.dzyuba.javaboost.domain.entities.lesson.Comment

class CommentsAdapter(private val userId: String) :
    ListAdapter<Comment, CommentsViewHolder>(CommentsItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val context = LayoutInflater.from(parent.context)
        return when(viewType){
            OUT_COMMENT_VIEW ->{
                CommentsViewHolder.OutCommentViewHolder(
                    RvOutCommentItemBinding.inflate(context, parent,false)
                )
            }
            IN_COMMENT_VIEW ->{
                CommentsViewHolder.InCommentViewHolder(
                    RvInCommentItemBinding.inflate(context, parent,false)
                )
            }
            else-> throw RuntimeException("Unresolved comment view type")
        }
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comment = getItem(position)
        when(holder){
            is CommentsViewHolder.OutCommentViewHolder -> holder.bind(comment)
            is CommentsViewHolder.InCommentViewHolder -> holder.bind(comment)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(getItem(position).userId != userId) OUT_COMMENT_VIEW else IN_COMMENT_VIEW

    }
    companion object{
        private const val OUT_COMMENT_VIEW = 0
        private const val IN_COMMENT_VIEW = 1
    }
}