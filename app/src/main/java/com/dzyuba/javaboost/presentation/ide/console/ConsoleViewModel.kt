package com.dzyuba.javaboost.presentation.ide.console

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.javaengine.JavaEngine
import com.xiaoyv.javaengine.compile.listener.ExecuteListener
import com.xiaoyv.javaengine.console.JavaConsole
import com.xiaoyv.javaengine.console.JavaConsole.AppendStdListener
import javax.inject.Inject

class ConsoleViewModel @Inject constructor(): ViewModel() {


    private val _stderr = MutableLiveData<List<CharSequence>>(listOf())
    val stderr: LiveData<List<CharSequence>>
        get() = _stderr

    private val _stdout = MutableLiveData<List<CharSequence>>(listOf())
    val stdout: LiveData<List<CharSequence>>
        get() = _stdout

    private val consoleOut = arrayListOf<String>()

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
            }
        })
    }


    fun runDexFile(dexPath: String, className: String, args: Array<String?>) {
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
        if (!StringUtils.isEmpty(className)) {
            JavaEngine.getDexExecutor().exec(dexPath, className, args, executeListener)
        } else {
            JavaEngine.getDexExecutor().exec(dexPath, args, executeListener)
        }
    }
}