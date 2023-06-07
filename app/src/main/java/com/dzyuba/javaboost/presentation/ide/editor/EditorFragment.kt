package com.dzyuba.javaboost.presentation.ide.editor

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentEditorBinding
import com.dzyuba.javaboost.databinding.FragmentEditorOuputBinding
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.presentation.ide.common.code.SymbolView
import com.dzyuba.javaboost.presentation.ide.common.utils.Lexer
import com.dzyuba.javaboost.presentation.ide.console.ConsoleData
import com.dzyuba.javaboost.presentation.ide.console.ConsoleFragment
import com.dzyuba.javaboost.presentation.ide.console.ConsoleFragment.Companion.CONSOLE_FRAGMENT_ANSWER
import com.dzyuba.javaboost.presentation.ide.console.ConsoleFragment.Companion.CONSOLE_FRAGMENT_RESULT
import com.dzyuba.javaboost.util.getSerializableStable
import com.xiaoyv.javaengine.JavaEngineSetting

class EditorFragment : Fragment() {


    private var _binding: FragmentEditorBinding? = null
    val binding: FragmentEditorBinding
        get() = _binding ?: throw RuntimeException("FragmentEditorBinding == null")

    private var _outputDialogBinding: FragmentEditorOuputBinding? = null
    private val outputDialogBinding: FragmentEditorOuputBinding
        get() = _outputDialogBinding ?: throw RuntimeException("FragmentEditorOuputBinding == null")

    private val viewModel by lazy {
        ViewModelProvider(this)[EditorViewModel::class.java]
    }

    private val practice by lazy {
        arguments?.getSerializableStable<Practice>(PRACTICE_KEY)!!
    }

    private var outputDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        setUI()
        setObservers()
        setFragmentResult()
    }

    private fun setFragmentResult() {
        parentFragmentManager.setFragmentResultListener(
            CONSOLE_FRAGMENT_RESULT, viewLifecycleOwner
        ) { key, bundle ->
            val answerPractice =
                bundle.getSerializableStable<Pair<Long, Boolean>>(CONSOLE_FRAGMENT_ANSWER)
            viewModel.practiceDecide = answerPractice
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.setFragmentResult(
                        EDITOR_FRAGMENT_RESULT, bundleOf(
                            EDITOR_FRAGMENT_ANSWER to viewModel.practiceDecide
                        )
                    )
                    parentFragmentManager.popBackStack()
                }

            })
    }

    private fun setObservers() {
        viewModel.importText.observe(viewLifecycleOwner) {
            val oldCaretPosition = binding.editorEditLayout.caretPosition
            val code = binding.editorEditLayout.text.toString();

            if (code.contains(it)) {
                return@observe
            }

            if (code.trim().contains("package")) {
                val tempPackage = code.substring(0, code.indexOf(";") + 1);
                binding.editorEditLayout.insert(tempPackage.length + 1, "\n" + it);
                binding.editorEditLayout.moveCaret(oldCaretPosition);
                binding.editorEditLayout.moveCaretDown();
                return@observe
            }
            binding.editorEditLayout.insert(0, it + "\n");
            binding.editorEditLayout.moveCaret(oldCaretPosition);
            binding.editorEditLayout.moveCaretDown();
        }
        viewModel.log.observe(viewLifecycleOwner) {
            showLog()
        }
        viewModel.normalInfo.observe(viewLifecycleOwner) {
            showNormalInfo(it)
        }
        viewModel.logDismiss.observe(viewLifecycleOwner) {
            showLogDismissListener(it)
        }
        viewModel.errorInfo.observe(viewLifecycleOwner) {
            showErrorInfo(it)
        }
        viewModel.showProgress.observe(viewLifecycleOwner) {
            showProgress(it.first, it.second)
        }
    }

    private fun initData() {
        binding.editorEditLayout.loadAppInfoJavaFile()
        ThreadUtils.getCachedPool().execute {
            Lexer.getLanguage().addRtIdentifier(JavaEngineSetting.getRtPath())
        }
    }

    private fun setUI() {
        _outputDialogBinding =
            FragmentEditorOuputBinding.inflate(LayoutInflater.from(requireActivity()), null, false)
        binding.editorSymbolView.setOnSymbolViewClick { view, text ->
            if (text.equals(SymbolView.TAB)) {
                binding.editorEditLayout.insert(binding.editorEditLayout.getCaretPosition(), "  ")
            } else {
                binding.editorEditLayout.insert(binding.editorEditLayout.getCaretPosition(), text)
            }
        }
        binding.editorEditLayout.setOnEditListener {
            binding.editorEditLayout.setEdited(false)
            return@setOnEditListener
        }
        binding.editorSymbolView.textBackgroundColor =
            requireContext().getColor(R.color.black_light)
        binding.editorSymbolView.textColor = requireContext().getColor(R.color.white)
        binding.editorEditLayout.setDark(true)
        binding.editorEditLayout.setKeywordColor(requireContext().getColor(R.color.orange))
        binding.editorEditLayout.setTextColor(Color.WHITE)
        binding.editorEditLayout.setImportBtnClickListener { selectedText, isHide ->
            viewModel.findPackage(
                selectedText,
                isHide
            )
        }
        binding.ivBuild.setOnClickListener {
            KeyboardUtils.hideSoftInput(binding.editorEditLayout)
            if (binding.editorEditLayout.isEmpty())
                return@setOnClickListener
            val edited: Boolean = binding.editorEditLayout.isEdited()
            if (edited) {
                binding.editorEditLayout.save()
            }
            binding.editorEditLayout.save()
            viewModel.runFile(binding.editorEditLayout.file)
        }
        binding.ivUndo.setOnClickListener {
            binding.editorEditLayout.undo()
        }
        binding.ivRedo.setOnClickListener {
            binding.editorEditLayout.redo()
        }
    }

    override fun onDestroy() {
        KeyboardUtils.unregisterSoftInputChangedListener(requireActivity().getWindow())
        super.onDestroy()
    }

    private fun showLog() {
        if (outputDialog == null) {
            outputDialogBinding.outputCloseView.setOnClickListener { v: View? -> outputDialog!!.dismiss() }
            outputDialogBinding.outputEditView.setOnClickListener { v: View? ->
                outputDialogBinding.outputConsole.setTextIsSelectable(true)
                outputDialogBinding.outputConsole.requestFocus()
                ToastUtils.showShort("Log replication mode enabled")
            }

            outputDialog = AlertDialog.Builder(requireActivity(), R.style.console_dialog)
                .setView(outputDialogBinding.root)
                .create()
            outputDialog!!.setCanceledOnTouchOutside(false)
        }
        outputDialog!!.show()
        outputDialogBinding.outputConsole.setText(null)
        outputDialogBinding.outputConsole.setTextIsSelectable(false)
        outputDialogBinding.outputCloseView.isEnabled = false
        outputDialogBinding.outputProgressBar.progress = 0
        outputDialogBinding.outputProgressBar.isIndeterminate = true
        outputDialog!!.setCancelable(false)
        val window = outputDialog!!.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.decorView.setPadding(0, 0, 0, 0)
            val display: Display = requireActivity().getWindowManager().getDefaultDisplay()
            val lp = window.attributes
            lp.width = display.width
            lp.height = display.height / 2
            window.attributes = lp
        }
    }

    private fun showProgress(task: String?, progress: Int) {
        if (!outputDialog!!.isShowing) outputDialog!!.show()
        outputDialogBinding.outputProgressBar.isIndeterminate = false
        outputDialogBinding.outputProgressBar.progress = progress
        outputDialogBinding.outputConsole.append(
            java.lang.String.format(
                ">>> Compilation progress: %s\n",
                FileUtils.getFileName(task)
            )
        )
    }

    private fun showErrorInfo(err: String) {
        if (!outputDialog!!.isShowing) outputDialog!!.show()
        outputDialogBinding.outputCloseView.isEnabled = true
        outputDialog!!.setOnDismissListener(null)
        outputDialogBinding.outputConsole.append(
            Html.fromHtml(
                "<br><font color=\"#FF0000\">" + err.replace(
                    "\n",
                    "<br>"
                ) + "</font>",
                FROM_HTML_MODE_COMPACT
            )
        )
    }

    private fun showNormalInfo(out: String?) {
        if (!outputDialog!!.isShowing) outputDialog!!.show()
        outputDialogBinding.outputConsole.append(out)
        outputDialogBinding.outputConsole.append("\n")
    }

    private fun showLogDismissListener(dexPath: String?) {
        outputDialogBinding.outputCloseView.isEnabled = true
        outputDialog!!.setOnDismissListener { dialog: DialogInterface? ->
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .add(
                    R.id.fragmentContainer,
                    ConsoleFragment.newInstance(
                        ConsoleData(
                            dexPath!!,
                            null,
                            binding.editorEditLayout.text.toString(),
                            practice
                        )
                    )
                )
                .commit()
        }
    }

    companion object {
        const val EDITOR_FRAGMENT_RESULT = "EDITOR_FRAGMENT_RESULT"
        const val EDITOR_FRAGMENT_ANSWER = "EDITOR_FRAGMENT_ANSWER"

        private const val PRACTICE_KEY = "PRACTICE_KEY"

        fun newInstance(practice: Practice) = EditorFragment().apply {
            arguments = bundleOf(PRACTICE_KEY to practice)
        }

    }
}