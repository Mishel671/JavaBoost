package com.dzyuba.javaboost.presentation.forgot_password

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
import com.dzyuba.javaboost.databinding.FragmentForgotPasswordBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showAlert
import com.dzyuba.javaboost.util.showErrorAlert
import javax.inject.Inject

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding: FragmentForgotPasswordBinding
        get() = _binding ?: throw RuntimeException("FragmentForgotPasswordBinding == null")

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ForgotPasswordViewModel::class.java]
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
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserves()
        setUI()
    }

    private fun setObserves() {
        viewModel.resetPassword.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                showAlert(
                    R.string.succes,
                    getString(R.string.forgot_password_email_sent_desc),
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
        viewModel.inputError.observe(viewLifecycleOwner) {
            binding.tilEmail.error =
                if (it) getString(R.string.forgot_password_uncorrect_email) else null

        }
    }

    private fun setUI() {
        arguments?.getString(INPUT_EMAIL)?.let {
            binding.etEmail.setText(it)
        }
        binding.btnSend.setOnClickListener {
            viewModel.sendPasswordByEmail(binding.etEmail.text?.toString())
        }
        binding.etEmail.doOnTextChanged { text, start, before, count ->
            viewModel.resetError()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.setFragmentResult(
                        FORGOT_PASSWORD_KEY,
                        bundleOf(FORGOT_PASSWORD_EMAIL_KEY to binding.etEmail.text?.toString())
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
        const val FORGOT_PASSWORD_KEY = "FORGOT_PASSWORD_KEY"
        const val FORGOT_PASSWORD_EMAIL_KEY = "FORGOT_PASSWORD_MAIL_KEY"

        private const val INPUT_EMAIL = "INPUT_EMAIL"

        fun newInstance(email: String?) = ForgotPasswordFragment().apply {
            arguments = Bundle().apply {
                putString(INPUT_EMAIL, email)
            }
        }
    }
}