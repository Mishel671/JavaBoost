package com.dzyuba.javaboost.presentation.lessons

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.ItemLessonShortBinding
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.util.gone
import com.dzyuba.javaboost.util.visible

class LessonsShortAdapter(private val context: Context) :
    ListAdapter<LessonShort, LessonsShortViewHolder>(LessonsShortDiffUtil) {

    var onItemClick: ((Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsShortViewHolder {
        val binding = ItemLessonShortBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LessonsShortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonsShortViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvDetailDescription.text = String.format(
            context.getString(R.string.lessons_list_description),
            item.detailDescription
        )
        setTags(binding, item.tags)
        binding.pbLessonProgress.progress = item.progress
        binding.root.setOnClickListener {
            onItemClick?.invoke(item.id)
        }
    }

    private fun setTags(binding: ItemLessonShortBinding, tags: List<LessonShort.Tags>) {
        binding.ivTheory.gone()
        binding.ivPractice.gone()
        binding.ivTest.gone()
        if (tags.isNotEmpty()) {
            tags.forEach {
                when (it) {
                    LessonShort.Tags.THEORY -> binding.ivTheory.visible()
                    LessonShort.Tags.PRACTICE -> binding.ivPractice.visible()
                    LessonShort.Tags.TEST -> binding.ivTest.visible()
                }
            }
        }
    }
}

class LessonsShortViewHolder(val binding: ItemLessonShortBinding) : ViewHolder(binding.root)

object LessonsShortDiffUtil : DiffUtil.ItemCallback<LessonShort>() {
    override fun areItemsTheSame(oldItem: LessonShort, newItem: LessonShort): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LessonShort, newItem: LessonShort): Boolean {
        return oldItem == newItem
    }
}