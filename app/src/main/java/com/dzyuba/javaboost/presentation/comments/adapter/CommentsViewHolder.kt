package com.dzyuba.javaboost.presentation.comments.adapter

import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.dzyuba.javaboost.databinding.RvHeaderItemBinding
import com.dzyuba.javaboost.databinding.RvInCommentItemBinding
import com.dzyuba.javaboost.databinding.RvOutCommentItemBinding
import com.dzyuba.javaboost.databinding.RvTextItemBinding
import com.dzyuba.javaboost.domain.entities.lesson.Comment
import com.dzyuba.javaboost.domain.entities.lesson.Header
import com.dzyuba.javaboost.domain.entities.lesson.Text
import com.dzyuba.javaboost.util.loadCircularImage

sealed class CommentsViewHolder(binding: ViewBinding) : ViewHolder(binding.root) {

    class OutCommentViewHolder(private val binding: RvOutCommentItemBinding) :
        CommentsViewHolder(binding) {
        fun bind(comment: Comment) {
            binding.ivLogo.loadCircularImage(comment.userLogo)
            binding.tvNickname.text = comment.userName
            binding.tvText.text = comment.text
        }
    }

    class InCommentViewHolder(private val binding: RvInCommentItemBinding) :
        CommentsViewHolder(binding) {
        fun bind(comment: Comment) {
            binding.ivLogo.loadCircularImage(comment.userLogo)
            binding.tvNickname.text = comment.userName
            binding.tvText.text = comment.text
        }
    }

}