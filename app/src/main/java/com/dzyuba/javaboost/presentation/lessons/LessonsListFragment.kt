package com.dzyuba.javaboost.presentation.lessons

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dzyuba.javaboost.databinding.FragmentLessonsListBinding

class LessonsListFragment : Fragment() {

    private var _binding: FragmentLessonsListBinding? = null
    private val binding: FragmentLessonsListBinding
        get() = _binding ?: throw RuntimeException("FragmentLessonsListBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = LessonsListFragment()
    }
}