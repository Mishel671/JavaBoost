package com.dzyuba.javaboost.presentation.email_verified

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentEmailVerifiedBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.util.*
import javax.inject.Inject

class EmailVerifiedFragment : Fragment() {

    private var _binding: FragmentEmailVerifiedBinding? = null
    private val binding: FragmentEmailVerifiedBinding
        get() = _binding ?: throw RuntimeException("FragmentEmailVerifiedBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[EmailVerifiedViewModel::class.java]
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerifiedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setUI()
    }

    private fun setObservers() {
        viewModel.emailIsVerified.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess { user ->
                dialog.dismiss()
                user?.let {
                    if (it.isEmailVerified) {
                        for (i in 0 until parentFragmentManager.backStackEntryCount) {
                            parentFragmentManager.popBackStack()
                        }
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_enter_left, R.anim.slide_exit_left)
                            .replace(R.id.fragmentContainer, NicknameFragment.launchRegistrationMode())
                            .commit()

                    } else {
                        showErrorAlert(Throwable(getString(R.string.email_verified_not_verify)))
                    }
                }
            }
        }
        viewModel.verificationEmail.observe(viewLifecycleOwner) {
            it.ifLoading {
                dialog.show()
            }.ifError {
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess {
                dialog.dismiss()
                showAlert(
                    R.string.succes,
                    String.format(
                        getString(R.string.email_verified_mail_sent),
                        viewModel.user.email
                    ),
                    R.string.ok
                )
                binding.btnContinue.visible()
                binding.btnSendMail.gone()
            }
        }
    }

    private fun setUI() {
        binding.tvDescription.text =
            String.format(getString(R.string.email_verified_desc), viewModel.user.email)
        binding.btnSendMail.setOnClickListener {
            viewModel.sendVerifyEmail()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.checkIsVerified()
        }
        binding.btnChangeMail.setOnClickListener {
            viewModel.logout()
            parentFragmentManager.popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                }
            })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = EmailVerifiedFragment()
    }
}