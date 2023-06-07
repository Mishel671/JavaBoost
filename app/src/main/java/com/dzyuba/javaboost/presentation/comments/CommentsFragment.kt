package com.dzyuba.javaboost.presentation.comments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.databinding.FragmentCommentsBinding
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.comments.adapter.CommentsAdapter
import com.dzyuba.javaboost.util.gone
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showErrorAlert
import com.dzyuba.javaboost.util.visible
import javax.inject.Inject


class CommentsFragment : Fragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding: FragmentCommentsBinding
        get() = _binding ?: throw RuntimeException("FragmentCommentsBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val lessonId by lazy {
        arguments?.getLong(LESSON_ID) ?: throw RuntimeException("Lesson id not put in arguments")
    }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[CommentsViewModel::class.java]
    }

    private val commentsAdapter by lazy { CommentsAdapter(viewModel.userId) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setUI()
    }

    private fun setObservers() {
        viewModel.subscribeToComments(lessonId)
        viewModel.lessonList.observe(viewLifecycleOwner) { comments ->
            comments.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                if (it?.isNotEmpty() == true) {
                    binding.tvEmptyComments.gone()
                    binding.rvComments.visible()
                    commentsAdapter.submitList(it)
                } else {
                    binding.tvEmptyComments.visible()
                    binding.rvComments.gone()
                }
            }
        }
        viewModel.sendComment.observe(viewLifecycleOwner) { response ->
            response.ifError {
                showErrorAlert(it)
            }.ifSuccess {
                binding.etComment.setText("")
            }
        }
    }

    private fun setUI() {
        binding.rvComments.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true)
        binding.rvComments.adapter = commentsAdapter
        binding.ivSend.isEnabled =
            binding.etComment.text.isNotEmpty() && viewModel.lessonList.value?.status == Resource.Status.SUCCESS
        binding.ivSend.setOnClickListener {
            viewModel.sendComment(lessonId, binding.etComment.text?.toString())
        }
        binding.etComment.doOnTextChanged { text, start, before, count ->
            binding.ivSend.isEnabled = text?.isNotEmpty() == true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LESSON_ID = "LESSON_ID"

        fun newInstance(lessonId: Long) = CommentsFragment().apply {
            arguments = bundleOf(LESSON_ID to lessonId)
        }
    }
}