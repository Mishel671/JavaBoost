package com.dzyuba.javaboost.presentation.signup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentSignUpBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showAlert
import com.dzyuba.javaboost.util.showErrorAlert
import javax.inject.Inject

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding: FragmentSignUpBinding
        get() = _binding ?: throw RuntimeException("FragmentSignUpBinding == null")

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[SignUpViewModel::class.java]
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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setUI()
    }

    private fun setObservers() {
        viewModel.inputErrors.observe(viewLifecycleOwner) {
            with(it) {
                binding.tilEmail.isErrorEnabled = errorEmail != null
                errorEmail?.let {
                    binding.tilEmail.error = getString(it)
                }
                binding.tilPassword.isErrorEnabled = errorPassword != null
                errorPassword?.let {
                    binding.tilPassword.error = getString(it)
                }
                binding.tilRepeatPassword.isErrorEnabled = errorRepeatPassword != null
                errorRepeatPassword?.let {
                    binding.tilRepeatPassword.error = getString(it)
                }
            }
        }
        viewModel.registration.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                showAlert(
                    R.string.succes,
                    getString(R.string.sign_up_user_register),
                    R.string.ok,
                    positiveAction = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    },
                    cancelAction = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
    }

    private fun setUI() {
        arguments?.getString(INPUT_EMAIL)?.let {
            binding.etEmail.setText(it)
        }
        binding.btnCreate.setOnClickListener {
            viewModel.singUp(
                binding.etEmail.text?.toString(),
                binding.etPassword.text?.toString(),
                binding.etRepeatPassword.text?.toString()
            )
        }
        binding.etEmail.doOnTextChanged { text, start, before, count ->
            viewModel.resetEmail()
        }
        binding.etPassword.doOnTextChanged { text, start, before, count ->
            viewModel.resetPassword()
        }
        binding.etRepeatPassword.doOnTextChanged { text, start, before, count ->
            viewModel.resetRepeatPassword()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.setFragmentResult(
                        SIGN_UP_KEY,
                        bundleOf(SIGN_UP_EMAIL_KEY to binding.etEmail.text?.toString())
                    )
                    parentFragmentManager.popBackStack()
                }
            })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val SIGN_UP_KEY = "SIGN_UP_KEY"
        const val SIGN_UP_EMAIL_KEY = "SIGN_UP_MAIL_KEY"

        private const val INPUT_EMAIL = "INPUT_EMAIL"

        fun newInstance(email: String?) = SignUpFragment().apply {
            arguments = Bundle().apply {
                putString(INPUT_EMAIL, email)
            }
        }
    }
}