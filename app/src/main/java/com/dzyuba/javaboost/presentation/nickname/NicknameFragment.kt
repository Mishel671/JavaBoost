package com.dzyuba.javaboost.presentation.nickname

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentNicknameBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.email_verified.EmailVerifiedViewModel
import com.dzyuba.javaboost.presentation.main.MainFragment
import com.dzyuba.javaboost.util.*
import javax.inject.Inject

class NicknameFragment : Fragment() {

    private var _binding: FragmentNicknameBinding? = null
    private val binding: FragmentNicknameBinding
        get() = _binding ?: throw RuntimeException("FragmentNicknameBinding == null")

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[NicknameViewModel::class.java]
    }

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setUI()
    }

    private fun setObservers() {
        viewModel.changeNickname.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_enter_left, R.anim.slide_exit_left)
                    .replace(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit()
            }
        }
        viewModel.inputError.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.tilNickname.isErrorEnabled = false
            } else {
                binding.tilNickname.isErrorEnabled = true
                binding.tilNickname.error = getString(it)
            }

        }
    }

    private fun setUI() {
        binding.btnSave.setOnClickListener {
            viewModel.setNickname(binding.etNickname.text?.toString())
        }
        binding.etNickname.doOnTextChanged { text, start, before, count ->
            viewModel.resetNickname()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = NicknameFragment()
    }
}