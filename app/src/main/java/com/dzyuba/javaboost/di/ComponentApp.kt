package com.dzyuba.javaboost.di

import android.app.Application
import com.dzyuba.javaboost.presentation.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [ViewModelModule::class, DataModule::class])
interface ComponentApp {

    fun inject(fragment: SplashFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ComponentApp
    }
}