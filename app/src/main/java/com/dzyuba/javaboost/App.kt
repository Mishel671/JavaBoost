package com.dzyuba.javaboost

import android.app.Application
import com.dzyuba.javaboost.di.DaggerComponentApp

class App : Application() {

    val componentApp by lazy {
        DaggerComponentApp.factory().create(this)
    }

}