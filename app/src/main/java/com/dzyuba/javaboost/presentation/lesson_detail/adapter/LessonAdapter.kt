package com.dzyuba.javaboost.presentation.lesson_detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.dzyuba.javaboost.databinding.RvCodeItemBinding
import com.dzyuba.javaboost.databinding.RvDividerItemBinding
import com.dzyuba.javaboost.databinding.RvHeaderItemBinding
import com.dzyuba.javaboost.databinding.RvPracticeItemBinding
import com.dzyuba.javaboost.databinding.RvTestItemBinding
import com.dzyuba.javaboost.databinding.RvTextItemBinding
import com.dzyuba.javaboost.domain.entities.lesson.*

class LessonAdapter : ListAdapter<LessonItem, LessonViewHolder>(LessonItemCallback) {

    var onLastItemView: (() -> Unit)? = null

    var onAnswerClick: ((Int, Long) -> Unit)? = null

    var onPracticeClick: ((Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER_VIEW -> LessonViewHolder.HeaderViewHolder(
                RvHeaderItemBinding.inflate(layoutInflater, parent, false)
            )

            TEXT_VIEW -> LessonViewHolder.TextViewHolder(
                RvTextItemBinding.inflate(layoutInflater, parent, false)
            )

            IMAGE_VIEW -> LessonViewHolder.DividerViewHolder(
                RvDividerItemBinding.inflate(layoutInflater, parent, false)
            )

            CODE_VIEW -> LessonViewHolder.CodeViewHolder(
                RvCodeItemBinding.inflate(layoutInflater, parent, false)
            )

            DIVIDER_VIEW -> LessonViewHolder.DividerViewHolder(
                RvDividerItemBinding.inflate(layoutInflater, parent, false)
            )

            TEST_VIEW -> LessonViewHolder.TestViewHolder(
                RvTestItemBinding.inflate(layoutInflater, parent, false)
            )

            PRACTICE_VIEW -> LessonViewHolder.PracticeViewHolder(
                RvPracticeItemBinding.inflate(layoutInflater, parent, false)
            )

            else -> throw RuntimeException("Unknown view type")
        }


    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        when (holder) {
            is LessonViewHolder.HeaderViewHolder -> holder.bind(getItem(position) as Header)
            is LessonViewHolder.TextViewHolder -> holder.bind(getItem(position) as Text)
            is LessonViewHolder.DividerViewHolder -> holder.bind(getItem(position) as Divider)
            is LessonViewHolder.CodeViewHolder -> holder.bind(getItem(position) as Code)
            is LessonViewHolder.TestViewHolder -> holder.bind(
                getItem(position) as Test,
                onClickAnswer = { answerId, itemId ->
                    onAnswerClick?.invoke(answerId, itemId)
                }
            )

            is LessonViewHolder.PracticeViewHolder -> holder.bind(
                getItem(position) as Practice,
                onClick = { practiceId ->
                    onPracticeClick?.invoke(practiceId)
                }
            )
        }
        if (position >= currentList.size - 1)
            onLastItemView?.invoke()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).type) {
            Type.HEADER -> HEADER_VIEW
            Type.TEXT -> TEXT_VIEW
            Type.IMAGE -> IMAGE_VIEW
            Type.CODE -> CODE_VIEW
            Type.DIVIDER -> DIVIDER_VIEW
            Type.TEST -> TEST_VIEW
            Type.PRACTICE -> PRACTICE_VIEW
        }
    }


    companion object {
        const val HEADER_VIEW = 0
        const val HEADER_VIEW_COUNT = 3
        const val TEXT_VIEW = 1
        const val TEXT_VIEW_COUNT = 12
        const val IMAGE_VIEW = 2
        const val IMAGE_VIEW_COUNT = 3
        const val CODE_VIEW = 3
        const val CODE_VIEW_COUNT = 2
        const val DIVIDER_VIEW = 4
        const val DIVIDER_VIEW_COUNT = 12
        const val TEST_VIEW = 5
        const val TEST_VIEW_COUNT = 4
        const val PRACTICE_VIEW = 6
        const val PRACTICE_VIEW_COUNT = 3
    }
}