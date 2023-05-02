package com.dzyuba.javaboost.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component
interface ComponentApp {


    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ComponentApp
    }
}