package com.dzyuba.javaboost.presentation.ide.console

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.dzyuba.javaboost.util.toConsoleOutput
import com.xiaoyv.javaengine.JavaEngine
import com.xiaoyv.javaengine.compile.listener.ExecuteListener
import com.xiaoyv.javaengine.console.JavaConsole
import com.xiaoyv.javaengine.console.JavaConsole.AppendStdListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConsoleViewModel @Inject constructor() : ViewModel() {


    private val _stderr = MutableLiveData<List<CharSequence>>(listOf())
    val stderr: LiveData<List<CharSequence>>
        get() = _stderr

    private val _stdout = MutableLiveData<List<CharSequence>>(listOf())
    val stdout: LiveData<List<CharSequence>>
        get() = _stdout

    private val _checkCode = MutableLiveData<CheckStatus>()
    val checkCode: LiveData<CheckStatus>
        get() = _checkCode

    private val consoleOut = arrayListOf<String>()

    private var outputIsValidate = false
    private var consoleData: ConsoleData? = null
    fun setup(consoleData: ConsoleData) {
        this.consoleData = consoleData
        if (consoleData.practice.outputKeyWords == null) {
            outputIsValidate = true
        }
    }

    private var validateJob: Job? = null

    private val javaConsole by lazy {
        JavaConsole(object : AppendStdListener {
            override fun printStderr(err: CharSequence) {
                Log.d("MainLog", "Console out error: $err")
                consoleOut.add(err.toString())
            }

            override fun printStdout(out: CharSequence) {
                Log.d("MainLog", "Console out success: $out")
                consoleOut.add(out.toString())
                _stdout.postValue(consoleOut)
                validateWork()
            }
        })
    }


    fun runDexFile() {
        javaConsole.start()
        val executeListener: ExecuteListener = object : ExecuteListener {
            override fun onExecuteFinish() {
                LogUtils.i("运行结束")
                javaConsole.stop()
            }

            override fun onExecuteError(error: Throwable) {
                LogUtils.e("运行错误：$error")
                javaConsole.stop()
            }
        }
        if (consoleData?.className?.isNotEmpty() == true) {
            JavaEngine.getDexExecutor()
                .exec(
                    consoleData!!.filePath,
                    consoleData?.className!!,
                    arrayOf(""),
                    executeListener
                )
        } else {
            JavaEngine.getDexExecutor().exec(consoleData!!.filePath, arrayOf(""), executeListener)
        }
    }


    private fun validateWork() {
        validateJob?.cancel()
        validateJob = viewModelScope.launch {
            delay(2000)
            var checkStatus = CheckStatus.SUCCESS
            consoleData!!.practice.outputKeyWords?.let { list ->
                val output = consoleOut.toConsoleOutput()
                list.forEach {
                    if (!output.contains(it, ignoreCase = true))
                        checkStatus = CheckStatus.CONSOLE_OUTPUT_ERROR
                }
            }
            consoleData!!.practice.codeKeyWords?.let { list ->
                list.forEach {
                    checkStatus = CheckStatus.CODE_ERROR
                }
            }
            _checkCode.postValue(checkStatus)
        }
    }

}