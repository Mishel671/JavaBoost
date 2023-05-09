package com.dzyuba.javaboost.presentation.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentMainBinding
import com.dzyuba.javaboost.presentation.lessons.LessonsListFragment
import com.dzyuba.javaboost.presentation.profile.ProfileFragment

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")

    private val lessonsFragment = LessonsListFragment.newInstance()
    private val profileFragment = ProfileFragment.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            launchNavigationFragments(lessonsFragment)
        }
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.lessons -> launchNavigationFragments(lessonsFragment)
                R.id.profile -> launchNavigationFragments(profileFragment)
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun launchNavigationFragments(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.bnvFragmentContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}