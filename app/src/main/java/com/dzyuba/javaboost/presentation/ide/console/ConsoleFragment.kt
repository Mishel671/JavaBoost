package com.dzyuba.javaboost.presentation.ide.console

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentConsoleBinding
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.ide.console.CheckStatus.*
import com.dzyuba.javaboost.presentation.ide.editor.EditorFragment
import com.dzyuba.javaboost.util.getSerializableStable
import com.dzyuba.javaboost.util.initLottieAnim
import com.dzyuba.javaboost.util.initProgressBar
import com.dzyuba.javaboost.util.showAlert
import com.dzyuba.javaboost.util.toConsoleOutput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.annotation.meta.When
import javax.inject.Inject

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null
    private val binding: FragmentConsoleBinding
        get() = _binding ?: throw RuntimeException("FragmentConsoleBinding == null")

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    private val successAnim by lazy { initLottieAnim(layoutInflater, requireContext(), 1000) }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ConsoleViewModel::class.java]
    }

    private val consoleData by lazy {
        arguments?.getSerializableStable<ConsoleData>(CONSOLE_DATA)!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConsoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    var answerResult: Pair<Long, Boolean>? = null
                    viewModel.checkCode.value?.let {
                        answerResult = Pair(consoleData.practice.id, it == SUCCESS)
                    }
                    parentFragmentManager.setFragmentResult(
                        CONSOLE_FRAGMENT_RESULT, bundleOf(
                            CONSOLE_FRAGMENT_ANSWER to answerResult
                        )
                    )
                    parentFragmentManager.popBackStack()
                }
            })
        viewModel.stderr.observe(viewLifecycleOwner) {
            binding.consoleOutput.text = ""
            it.forEach {
                binding.consoleOutput.append(Html.fromHtml("<font color='#F00'>$it</font>"))
            }
        }
        viewModel.stdout.observe(viewLifecycleOwner) { listOut ->
            binding.consoleOutput.text = ""
            binding.consoleOutput.text = listOut.toConsoleOutput()
            Log.d("MainLog", "Set text ${listOut.toConsoleOutput()}, list: ${listOut}")
        }
        viewModel.stdout.observe(viewLifecycleOwner) { listOut ->
            binding.consoleOutput.text = ""
            binding.consoleOutput.text = listOut.toConsoleOutput()
            Log.d("MainLog", "Set text ${listOut.toConsoleOutput()}, list: ${listOut}")
        }
        viewModel.checkCode.observe(viewLifecycleOwner) {
            when (it) {
                SUCCESS -> successAnim.show()
                CODE_ERROR -> showAlert(
                    R.string.task_error,
                    getString(R.string.code_error),
                    R.string.ok,
                    positiveAction = { requireActivity().onBackPressedDispatcher.onBackPressed() }
                )

                CONSOLE_OUTPUT_ERROR -> showAlert(
                    R.string.task_error,
                    getString(R.string.console_output_error),
                    R.string.ok,
                    positiveAction = { requireActivity().onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
        viewModel.setup(consoleData)
        viewModel.runDexFile()
    }

    companion object {

        const val CONSOLE_FRAGMENT_RESULT = "CONSOLE_FRAGMENT_RESULT"
        const val CONSOLE_FRAGMENT_ANSWER = "CONSOLE_FRAGMENT_ANSWER"

        private const val CONSOLE_DATA = "CONSOLE_DATA"

        fun newInstance(consoleData: ConsoleData) = ConsoleFragment().apply {
            arguments = bundleOf(CONSOLE_DATA to consoleData)
        }
    }

}