package com.dzyuba.javaboost.presentation.signin

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentSignInBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.email_verified.EmailVerifiedFragment
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment.Companion.FORGOT_PASSWORD_KEY
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment.Companion.FORGOT_PASSWORD_EMAIL_KEY
import com.dzyuba.javaboost.presentation.main.MainFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.presentation.signup.SignUpFragment
import com.dzyuba.javaboost.presentation.signup.SignUpFragment.Companion.SIGN_UP_KEY
import com.dzyuba.javaboost.presentation.signup.SignUpFragment.Companion.SIGN_UP_EMAIL_KEY
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showErrorAlert
import javax.inject.Inject

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding: FragmentSignInBinding
        get() = _binding ?: throw RuntimeException("FragmentSignInBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[SignInViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.getBoolean(OPEN_EMAIL_VERIFICATION) == true && viewModel.isAuthenticated())
            launchFragmentWithReturn(EmailVerifiedFragment.newInstance())
        setFragmentsListener()
        setObservers()
        setUI()
    }

    private fun setFragmentsListener() {
        parentFragmentManager.setFragmentResultListener(
            FORGOT_PASSWORD_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val email = bundle.getString(FORGOT_PASSWORD_EMAIL_KEY) ?: ""
            binding.etEmail.setText(email)
        }
        parentFragmentManager.setFragmentResultListener(
            SIGN_UP_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val email = bundle.getString(SIGN_UP_EMAIL_KEY) ?: ""
            binding.etEmail.setText(email)
        }
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
            }
        }
        viewModel.signIn.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                it.ifLoading {
                    dialog.show()
                }.ifError {
                    dialog.dismiss()
                    showErrorAlert(it)
                }.ifSuccess { user ->
                    dialog.dismiss()
                    user?.let {
                        val fragmentTransaction =
                            parentFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_enter_left,
                                    R.anim.slide_exit_left,
                                    R.anim.slide_enter_right,
                                    R.anim.slide_exit_right
                                )

                        val needFragment = if (user.isEmailVerified) {
                            if (user.name != null)
                                MainFragment.newInstance()
                            else
                                NicknameFragment.launchRegistrationMode()
                        } else {
                            fragmentTransaction.addToBackStack(null)
                            EmailVerifiedFragment.newInstance()
                        }
                        fragmentTransaction
                            .replace(R.id.fragmentContainer, needFragment)
                            .commit()
                    }
                }
            }
        }
    }

    private fun setUI() {
        binding.btnForgotPassword.setOnClickListener {
            launchFragmentWithReturn(
                ForgotPasswordFragment.newInstance(binding.etEmail.text?.toString())
            )
        }
        binding.btnSignIn.setOnClickListener {
            viewModel.signIn(binding.etEmail.text?.toString(), binding.etPassword.text?.toString())
        }
        binding.btnRegistration.setOnClickListener {
            launchFragmentWithReturn(
                SignUpFragment.newInstance(binding.etEmail.text?.toString())
            )
        }
        binding.etEmail.doOnTextChanged { text, start, before, count ->
            viewModel.resetEmail()
        }
        binding.etPassword.doOnTextChanged { text, start, before, count ->
            viewModel.resetPassword()
        }
    }

    private fun launchFragmentWithReturn(fragment: Fragment, backStackKey: String? = null) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_enter_left,
                R.anim.slide_exit_left,
                R.anim.slide_enter_right,
                R.anim.slide_exit_right
            )
            .addToBackStack(backStackKey)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val OPEN_EMAIL_VERIFICATION = "OPEN_EMAIL_VERIFICATION"

        fun newInstance() = SignInFragment()
        fun newInstanceToEmailVerified() = SignInFragment().apply {
            arguments = bundleOf(OPEN_EMAIL_VERIFICATION to true)
        }
    }
}