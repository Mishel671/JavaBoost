package com.dzyuba.javaboost.presentation.lesson_detail.adapter

import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.dzyuba.javaboost.databinding.RvCodeItemBinding
import com.dzyuba.javaboost.databinding.RvDividerItemBinding
import com.dzyuba.javaboost.databinding.RvHeaderItemBinding
import com.dzyuba.javaboost.databinding.RvTextItemBinding
import com.dzyuba.javaboost.domain.entities.lesson.Code
import com.dzyuba.javaboost.domain.entities.lesson.Divider
import com.dzyuba.javaboost.domain.entities.lesson.Header
import com.dzyuba.javaboost.domain.entities.lesson.Text
import com.dzyuba.javaboost.util.DarkBackgroundCustomScheme
import com.dzyuba.javaboost.util.convertDpToPixels
import com.dzyuba.javaboost.util.gone
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
}