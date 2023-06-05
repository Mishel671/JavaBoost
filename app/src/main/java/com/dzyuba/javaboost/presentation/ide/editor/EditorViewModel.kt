package com.dzyuba.javaboost.presentation.ide.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.dzyuba.javaboost.presentation.ide.common.utils.Lexer
import com.xiaoyv.javaengine.JavaEngine
import com.xiaoyv.javaengine.compile.listener.CompilerListener
import java.io.File
import javax.inject.Inject

class EditorViewModel @Inject constructor(): ViewModel() {

    private val _importText = MutableLiveData<String>()
    val importText: LiveData<String>
        get() = _importText

    private val _log = MutableLiveData<Unit>()
    val log: LiveData<Unit>
        get() = _log

    private val _normalInfo = MutableLiveData<String>()
    val normalInfo: LiveData<String>
        get() = _normalInfo

    private val _logDismiss = MutableLiveData<String>()
    val logDismiss: LiveData<String>
        get() = _logDismiss

    private val _errorInfo = MutableLiveData<String>()
    val errorInfo: LiveData<String>
        get() = _errorInfo

    private val _showProgress = MutableLiveData<Pair<String, Int>>()
    val showProgress: LiveData<Pair<String, Int>>
        get() = _showProgress

    fun findPackage(selectedText: String, isHide: Boolean) {
        // 若选择的非单词，则跳过
        if (selectedText.trim { it <= ' ' }.contains(" ")) {
            return
        }
        val identifier: HashMap<String, String> = Lexer.getLanguage().getIdentifier()
        val className: String?
        if (identifier.containsKey(selectedText)) {
            className = identifier[selectedText]
            LogUtils.e("找到:$className")
        } else {
            if (!isHide) ToastUtils.showShort("JDK 中未找到该类:$selectedText")
            return
        }
        val importText = "import $className;"
        _importText.value = importText
    }

    fun runFile(javaFile: File?) {
        _log.value = Unit
        _normalInfo.value =
            "Tips：The initial compilation needs to load the environment\n\n>>> compile start"
        val saveClassDir = File(PathUtils.getExternalAppCachePath() + "/class")
        val saveDexFile = File(PathUtils.getExternalAppCachePath() + "/dex/temp.dex")

        JavaEngine.getClassCompiler()
            .compile(javaFile!!, saveClassDir, object : CompilerListener() {
                override fun onSuccess(path: String) {
                    ThreadUtils.runOnUiThread {
                        _normalInfo.value =
                            String.format(
                                ">>> end of compilation\n>>> output path：%s\n>>> conversion started",
                                path.replace(
                                    PathUtils.getExternalAppDataPath(), ""
                                )
                            )

                    }

                    JavaEngine.getDexCompiler()
                        .compile(path, saveDexFile.absolutePath, object : CompilerListener() {
                            override fun onSuccess(path: String) {
                                ThreadUtils.runOnUiThread {
                                    _normalInfo.value =
                                        String.format(
                                            ">>> conversion complete\n>>> output path：%s\nTips：Please close the log box to run the program",
                                            path.replace(
                                                PathUtils.getExternalAppDataPath(), ""
                                            )
                                        )

                                    _logDismiss.value = path
                                }
                            }

                            override fun onError(error: Throwable) {
                                ThreadUtils.runOnUiThread {
                                    _errorInfo.value = error.message
                                }
                            }

                            override fun onProgress(task: String, progress: Int) {
                                _showProgress.value = Pair(
                                    task.replace(
                                        PathUtils.getExternalAppDataPath(), ""
                                    ), progress
                                )
                            }
                        })
                }

                override fun onError(error: Throwable) {
                    ThreadUtils.runOnUiThread {
                        _errorInfo.value = error.message

                    }
                }

                override fun onProgress(task: String, progress: Int) {
                    _showProgress.value = Pair(
                        task.replace(PathUtils.getExternalAppDataPath(), ""), progress
                    )
                }
            })
    }

}