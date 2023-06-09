package com.dzyuba.javaboost.presentation.lesson_detail

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentLessonDetailBinding
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.comments.CommentsFragment
import com.dzyuba.javaboost.presentation.ide.editor.EditorFragment
import com.dzyuba.javaboost.presentation.ide.editor.EditorFragment.Companion.EDITOR_FRAGMENT_ANSWER
import com.dzyuba.javaboost.presentation.ide.editor.EditorFragment.Companion.EDITOR_FRAGMENT_RESULT
import com.dzyuba.javaboost.presentation.lesson_detail.adapter.LessonAdapter
import com.dzyuba.javaboost.util.getSerializableStable
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showAlert
import com.dzyuba.javaboost.util.showErrorAlert
import javax.inject.Inject

class LessonDetailFragment : Fragment() {

    private var _binding: FragmentLessonDetailBinding? = null
    private val binding: FragmentLessonDetailBinding
        get() = _binding ?: throw RuntimeException("FragmentLessonDetailBinding == null")


    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val lessonId by lazy {
        arguments?.getLong(LESSON_ID_KEY)!!
    }

    private val lessonAdapter = LessonAdapter()

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[LessonDetailViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.setExpanded(viewModel.toolbarIsExpanded)
        viewModel.loadLesson(lessonId)
        setObservers()
        setupAdapter()
        setFragmentResult()
    }

    private fun setFragmentResult() {
        parentFragmentManager.setFragmentResultListener(
            EDITOR_FRAGMENT_RESULT, viewLifecycleOwner) { key, bundle ->
            val answerPractice = bundle.getSerializableStable<Pair<Long, Boolean>>(EDITOR_FRAGMENT_ANSWER)
            viewModel.setPracticeAnswer(answerPractice)
        }
    }

    private fun setupAdapter() {
        binding.rvLesson.recycledViewPool.apply {
            setMaxRecycledViews(LessonAdapter.HEADER_VIEW, LessonAdapter.HEADER_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.TEXT_VIEW, LessonAdapter.TEXT_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.IMAGE_VIEW, LessonAdapter.IMAGE_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.CODE_VIEW, LessonAdapter.CODE_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.DIVIDER_VIEW, LessonAdapter.DIVIDER_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.TEST_VIEW, LessonAdapter.TEST_VIEW_COUNT)
            setMaxRecycledViews(LessonAdapter.PRACTICE_VIEW, LessonAdapter.PRACTICE_VIEW_COUNT)
        }
        binding.rvLesson.adapter = lessonAdapter
        lessonAdapter.onLastItemView = {
            viewModel.setRead()
        }
        lessonAdapter.onAnswerClick = { answerId, itemId ->
            viewModel.setAnswer(answerId, itemId)
        }
        lessonAdapter.onPracticeClick = { practiceId ->
            val practice = viewModel.lesson.value?.data?.lessonItems?.get(practiceId.toInt()) as? Practice
            practice?.let {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_enter_left,
                        R.anim.slide_exit_left,
                        R.anim.slide_enter_right,
                        R.anim.slide_exit_right
                    ).addToBackStack(null)
                    .replace(R.id.bnvFragmentContainer, EditorFragment.newInstance(practice))
                    .commit()
            }
        }
    }

    private fun setObservers() {
        viewModel.lesson.observe(viewLifecycleOwner) { it ->
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                it?.let {
                    setUI(it)
                }
            }
        }
        viewModel.rate.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                showAlert(
                    R.string.succes,
                    getString(R.string.lesson_detail_thanks_for_rate),
                    R.string.ok,
                    positiveAction = {
                        viewModel.loadLesson(lessonId)
                    },
                    cancelAction = {
                        viewModel.loadLesson(lessonId)
                    }
                )
            }
        }
    }

    private fun setUI(lesson: Lesson) {
        binding.tvLessonName.text = lesson.lessonName
        val userRate = lesson.rating?.get(viewModel.userId)
        binding.tvYourRate.text = if (userRate == null)
            getString(R.string.lesson_detail_set_rating)
        else {
            binding.rbRating.rating = userRate
            String.format(getString(R.string.lesson_detail_your_rating), userRate)
        }
        val middleRate = lesson.rating?.values?.average() ?: 0.0
        binding.tvMiddleRate.text =
            String.format(getString(R.string.lesson_detail_middle_rating), middleRate)
        binding.rbRating.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser)
                viewModel.setRate(lessonId, rating)
        }
        binding.includeComments.leftText.text = getString(R.string.lesson_detail_comments)
        binding.includeComments.root.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_enter_left,
                    R.anim.slide_exit_left,
                    R.anim.slide_enter_right,
                    R.anim.slide_exit_right
                ).addToBackStack(null)
                .replace(R.id.bnvFragmentContainer, CommentsFragment.newInstance(lesson.id))
                .commit()
        }
        lessonAdapter.submitList(lesson.lessonItems)
    }


    override fun onDestroyView() {
        viewModel.toolbarIsExpanded = !binding.appBar.isLifted
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LESSON_ID_KEY = "LESSON_ID_KEY"

        fun newInstance(lessonId: Long) = LessonDetailFragment().apply {
            arguments = bundleOf(LESSON_ID_KEY to lessonId)
        }
    }
}