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
import com.dzyuba.javaboost.presentation.ide.common.code.SymbolView
import com.dzyuba.javaboost.presentation.ide.common.utils.Lexer
import com.dzyuba.javaboost.presentation.ide.console.ConsoleFragment
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
//        KeyboardUtils.registerSoftInputChangedListener(requireActivity().getWindow()) { height ->
//            val params =
//                binding.editorSymbolView.getLayoutParams() as ConstraintLayout.LayoutParams
//            params.bottomMargin = height - ConvertUtils.dp2px(56F)
//            binding.editorSymbolView.setLayoutParams(params)
//            binding.editorSymbolView.setVisible(false)
//        }
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
            viewModel.runFile(binding.editorEditLayout.getFile())
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


//    fun formatCodeFail(exception: FormatterException) {
//        val diagnostics: List<FormatterDiagnostic> = exception.diagnostics()
//        if (ObjectUtils.isNotEmpty(diagnostics)) {
//            val diagnostic: FormatterDiagnostic = diagnostics[0]
//            val line: Int = diagnostic.line()
//            val column: Int = diagnostic.column()
//            var message: String = diagnostic.message()
//            message = "line:$line\ncolumn:$column\nerror:$message"
//            val alertDialog = AlertDialog.Builder(activity)
//                .setTitle("Alignment failed")
//                .setMessage(message)
//                .setPositiveButton("Ok") { dialog: DialogInterface?, which: Int ->
//                    editorEditLayout.gotoLine(line)
//                    val caretPosition: Int = editorEditLayout.getCaretPosition()
//                    editorEditLayout.moveCaret(caretPosition + column)
//                }
//                .create()
//            alertDialog.setCanceledOnTouchOutside(false)
//            alertDialog.show()
//        }
//    }
//
//    fun formatCodeSuccess(sourceCode: String?) {
//        editorEditLayout.setText(sourceCode)
//        editorEditLayout.save()
//    }

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
                .add(R.id.fragmentContainer, ConsoleFragment.newInstance(dexPath!!, null))
                .commit()
        }
    }

    companion object {

        private const val OUTPUT_KEY_WORDS = "OUTPUT_KEY_WORDS"

        fun newInstance(outputKeyWords: List<String>) = EditorFragment().apply {
            arguments = bundleOf(OUTPUT_KEY_WORDS to outputKeyWords)
        }

    }
}