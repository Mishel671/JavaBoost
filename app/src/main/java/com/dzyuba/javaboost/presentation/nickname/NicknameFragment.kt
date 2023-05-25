package com.dzyuba.javaboost.presentation.nickname

import android.content.Context
import android.os.Build
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
import com.dzyuba.javaboost.databinding.FragmentNicknameBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment
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

    private val mode by lazy {
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arguments?.getSerializable(MODE_KEY, Mode::class.java)
        else
            arguments?.getSerializable(MODE_KEY) as Mode
        return@lazy value ?: throw RuntimeException("Unknown mode for nickname screen")

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
                if (mode == Mode.REGISTRATION) {
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_enter_left, R.anim.slide_exit_left)
                        .replace(R.id.fragmentContainer, MainFragment.newInstance())
                        .commit()
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
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
        if (mode == Mode.EDIT) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        parentFragmentManager.setFragmentResult(
                            EDIT_RESULT_KEY, bundleOf()
                        )
                        parentFragmentManager.popBackStack()
                    }
                })
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {

        private const val MODE_KEY = "MODE_KEY"
        const val EDIT_RESULT_KEY = "EDIT_RESULT_KEY"

        fun launchRegistrationMode() = NicknameFragment().apply {
            arguments = bundleOf(MODE_KEY to Mode.REGISTRATION)
        }

        fun launchEditMode() = NicknameFragment().apply {
            arguments = bundleOf(MODE_KEY to Mode.EDIT)
        }
    }
}