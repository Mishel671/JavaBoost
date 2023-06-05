package com.dzyuba.javaboost.presentation.ide.console

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.databinding.FragmentConsoleBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import javax.inject.Inject

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null
    private val binding: FragmentConsoleBinding
        get() = _binding ?: throw RuntimeException("FragmentConsoleBinding == null")

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ConsoleViewModel::class.java]
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
        viewModel.stderr.observe(viewLifecycleOwner) {
            binding.consoleOutput.text = ""
            it.forEach {
                binding.consoleOutput.append(Html.fromHtml("<font color='#F00'>$it</font>"))
            }
        }
        viewModel.stdout.observe(viewLifecycleOwner) { listOut ->
            binding.consoleOutput.text = ""
            var text = ""
            listOut.forEach {
                text += it.toString() + "\n"
            }
            binding.consoleOutput.text = text
            Log.d("MainLog", "Set text ${text}, list: ${listOut}")
        }
        val path = arguments?.getString(FILE_PATH)!!
        val className = arguments?.getString(CLASS_NAME) ?: ""
        viewModel.runDexFile(path, className, arrayOf(""))
    }

    companion object {
        private const val FILE_PATH = "FILE_PATH"
        private const val CLASS_NAME = "CLASS_NAME"

        fun newInstance(filePath: String, className: String?) = ConsoleFragment().apply {
            arguments = bundleOf(FILE_PATH to filePath, CLASS_NAME to className)
        }
    }

}