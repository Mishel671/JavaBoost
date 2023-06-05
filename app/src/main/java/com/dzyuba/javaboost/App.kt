package com.dzyuba.javaboost

import com.dzyuba.javaboost.di.DaggerComponentApp
import com.xiaoyv.javaengine.JavaEngineApplication

class App : JavaEngineApplication() {

    val componentApp by lazy {
        DaggerComponentApp.factory().create(this)
    }

}