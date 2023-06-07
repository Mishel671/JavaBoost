package com.dzyuba.javaboost.presentation.decided_lessons

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentDecidedLessonsBinding
import com.dzyuba.javaboost.databinding.FragmentLessonsListBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.lesson_detail.LessonDetailFragment
import com.dzyuba.javaboost.presentation.lessons.LessonsListFragment
import com.dzyuba.javaboost.presentation.lessons.LessonsListViewModel
import com.dzyuba.javaboost.presentation.lessons.LessonsShortAdapter
import com.dzyuba.javaboost.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class DecidedLessonsFragment : Fragment() {

    private var _binding: FragmentDecidedLessonsBinding? = null
    private val binding: FragmentDecidedLessonsBinding
        get() = _binding ?: throw RuntimeException("FragmentDecidedLessonsBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val adapter by lazy { LessonsShortAdapter(requireContext()) }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    private val animJob: Job? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[DecidedLessonsViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecidedLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDecidedLessons()
        setObservers()
        setUI()
    }


    private fun setObservers() {
        viewModel.lessons.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess { list ->
                dialog.dismiss()
                if (list != null && list.isNotEmpty()) {
                    binding.tvListEmpty.gone()
                    binding.rvLessons.visible()
                    adapter.submitList(list)
                } else {
                    binding.tvListEmpty.visible()
                    binding.rvLessons.gone()
                }
            }
        }
        var oldProgressValue = 0
        viewModel.lessonsProgress.observe(viewLifecycleOwner) {
            val animator = ValueAnimator.ofInt(oldProgressValue, it).apply {
                duration = 1500
                interpolator = DecelerateInterpolator()
            }
            animator.addUpdateListener {
                if (isVisible) {
                    val progress = animator.animatedValue.toString().toInt()
                    binding.progressBar.progress = progress.toFloat()
                    binding.tvProgress.text = "$progress%"
                }
            }
            oldProgressValue = it
            animator.start()

        }
    }

    private fun setUI() {
        binding.rvLessons.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance() = DecidedLessonsFragment()

    }
}