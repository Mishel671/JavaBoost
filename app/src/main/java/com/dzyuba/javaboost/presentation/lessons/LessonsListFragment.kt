package com.dzyuba.javaboost.presentation.lessons

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentLessonsListBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.lesson_detail.LessonDetailFragment
import com.dzyuba.javaboost.presentation.nickname.Mode
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.util.*
import javax.inject.Inject

class LessonsListFragment : Fragment() {

    private var _binding: FragmentLessonsListBinding? = null
    private val binding: FragmentLessonsListBinding
        get() = _binding ?: throw RuntimeException("FragmentLessonsListBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val adapter by lazy { LessonsShortAdapter(requireContext()) }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[LessonsListViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ids = arguments?.getSerializableStable(LESSONS_IDS) as List<Int>?
        viewModel.loadLessons(ids)
        setObservers()
        setUI()
        showNavBar()
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
    }

    private fun setUI() {
        binding.rvLessons.adapter = adapter
        adapter.onItemClick = {
            hideNavBar()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_enter_left,
                    R.anim.slide_exit_left,
                    R.anim.slide_enter_right,
                    R.anim.slide_exit_right
                )
                .addToBackStack(null)
                .replace(R.id.bnvFragmentContainer, LessonDetailFragment.newInstance(it))
                .commit()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LESSONS_IDS = "LESSONS_IDS"

        fun allLessons() = LessonsListFragment()

        fun lessonsById(ids: List<Int>) = LessonsListFragment().apply {
            arguments = bundleOf(LESSONS_IDS to ids)
        }
    }
}