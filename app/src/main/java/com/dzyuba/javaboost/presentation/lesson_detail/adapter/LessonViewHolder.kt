package com.dzyuba.javaboost.presentation.lesson_detail.adapter

import android.graphics.Color
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.RvCodeItemBinding
import com.dzyuba.javaboost.databinding.RvDividerItemBinding
import com.dzyuba.javaboost.databinding.RvHeaderItemBinding
import com.dzyuba.javaboost.databinding.RvPracticeItemBinding
import com.dzyuba.javaboost.databinding.RvTestItemBinding
import com.dzyuba.javaboost.databinding.RvTextItemBinding
import com.dzyuba.javaboost.domain.entities.lesson.Code
import com.dzyuba.javaboost.domain.entities.lesson.Divider
import com.dzyuba.javaboost.domain.entities.lesson.Header
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.domain.entities.lesson.Test
import com.dzyuba.javaboost.domain.entities.lesson.Text
import com.dzyuba.javaboost.util.DarkBackgroundCustomScheme
import com.dzyuba.javaboost.util.convertDpToPixels
import com.dzyuba.javaboost.util.gone
import com.dzyuba.javaboost.util.invisible
import com.dzyuba.javaboost.util.visible
import de.markusressel.kodehighlighter.core.util.SpannableHighlighter
import de.markusressel.kodehighlighter.language.java.JavaRuleBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LessonViewHolder(binding: ViewBinding) : ViewHolder(binding.root) {

    class HeaderViewHolder(private val binding: RvHeaderItemBinding) : LessonViewHolder(binding) {
        fun bind(header: Header) {
            binding.root.text = Html.fromHtml(header.text, FROM_HTML_MODE_COMPACT)
        }
    }

    class TextViewHolder(private val binding: RvTextItemBinding) : LessonViewHolder(binding) {
        fun bind(text: Text) {
            binding.root.text = Html.fromHtml(text.text, FROM_HTML_MODE_COMPACT)
        }
    }

    class DividerViewHolder(private val binding: RvDividerItemBinding) : LessonViewHolder(binding) {
        fun bind(divider: Divider) {
            binding.root.dividerThickness = binding.convertDpToPixels(divider.heightInDp).toInt()
        }
    }

    class CodeViewHolder(private val binding: RvCodeItemBinding) : LessonViewHolder(binding) {
        fun bind(code: Code) {
            val javaRuleBook = JavaRuleBook()
            val javaHighlighter = SpannableHighlighter(javaRuleBook, DarkBackgroundCustomScheme())
            CoroutineScope(Dispatchers.Main).launch {
                val spannable = withContext(Dispatchers.Default) {
                    val spannable = Html.fromHtml(code.text, FROM_HTML_MODE_COMPACT).toSpannable()
                    javaHighlighter.highlight(spannable)
                    spannable
                }
                binding.tvCode.text = spannable
            }
            if (code.description != null) {
                binding.tvDescription.text = Html.fromHtml(code.description, FROM_HTML_MODE_COMPACT)
                binding.tvDescription.visible()
            } else {
                binding.tvDescription.gone()
            }
        }
    }

    class TestViewHolder(private val binding: RvTestItemBinding) : LessonViewHolder(binding) {
        fun bind(test: Test, onClickAnswer: ((Int, Long) -> Unit)) {
            with(binding) {
                tvQuestion.text = Html.fromHtml(test.question, FROM_HTML_MODE_COMPACT)
                rbQuestion1.text = test.answers[0].answer
                rbQuestion2.text = test.answers[1].answer
                rbQuestion3.text = test.answers[2].answer
                rbQuestion4.text = test.answers[3].answer
                if (test.answerResult == null) {
                    ivResult.invisible()
                    rgTest.canClick(true)
                } else {
                    if (test.answerResult == test.trueAnswerId) {
                        ivResult.setImageResource(R.drawable.ic_succes)
                        ivResult.setColorFilter(root.context.getColor(R.color.green))
                        ivResult.visible()
                        rgTest.canClick(false)
                    } else {
                        ivResult.setImageResource(R.drawable.ic_error)
                        ivResult.setColorFilter(root.context.getColor(R.color.red))
                        ivResult.visible()
                        rgTest.canClick(true)
                    }
                    rgTest.check(
                        when (test.answerResult) {
                            0 -> R.id.rbQuestion1
                            1 -> R.id.rbQuestion2
                            2 -> R.id.rbQuestion3
                            3 -> R.id.rbQuestion4
                            else -> throw RuntimeException("Unresolved radio button id")
                        }
                    )
                }
                btnConfirm.isEnabled = rgTest.checkedRadioButtonId != -1
                rgTest.setOnCheckedChangeListener { group, checkedId ->
                    btnConfirm.isEnabled = true
                }
                btnConfirm.setOnClickListener {
                    val answerId = when (rgTest.checkedRadioButtonId) {
                        R.id.rbQuestion1 -> test.answers[0].id
                        R.id.rbQuestion2 -> test.answers[1].id
                        R.id.rbQuestion3 -> test.answers[2].id
                        R.id.rbQuestion4 -> test.answers[3].id
                        else -> throw RuntimeException("Unresolved radio button id")
                    }
                    onClickAnswer.invoke(answerId, test.id)
                }
            }
        }

        private fun RadioGroup.canClick(isClickable: Boolean) {
            binding.rbQuestion1.isClickable = isClickable
            binding.rbQuestion2.isClickable = isClickable
            binding.rbQuestion3.isClickable = isClickable
            binding.rbQuestion4.isClickable = isClickable
        }
    }

    class PracticeViewHolder(private val binding: RvPracticeItemBinding) :
        LessonViewHolder(binding) {
        fun bind(practice: Practice, onClick: ((Long) -> Unit)? = null) {
            with(binding) {
                tvQuestion.text = practice.task
                if (practice.inputFormat != null) {
                    tvInputValuesTitle.visible()
                    tvInputValues.visible()
                    tvInputValues.text = practice.inputFormat
                } else {
                    tvInputValuesTitle.gone()
                    tvInputValues.gone()
                }
                if (practice.outputFormat != null) {
                    tvOutputValuesTitle.visible()
                    tvOutputValues.visible()
                    tvOutputValues.text = practice.outputFormat
                } else {
                   tvOutputValuesTitle.gone()
                   tvOutputValues.gone()
                }
                btnPractice.setOnClickListener {
                    onClick?.invoke(practice.id)
                }
                if (practice.wasDecided != null) {
                    ivResult.visible()
                    if (practice.wasDecided) {
                        ivResult.setImageResource(R.drawable.ic_succes)
                        ivResult.setColorFilter(root.context.getColor(R.color.green))
                    } else {
                        ivResult.setImageResource(R.drawable.ic_error)
                        ivResult.setColorFilter(root.context.getColor(R.color.red))
                    }
                } else {
                    ivResult.gone()
                }
            }
        }
    }
}