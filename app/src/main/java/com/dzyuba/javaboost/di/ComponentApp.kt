package com.dzyuba.javaboost.di

import android.app.Application
import com.dzyuba.javaboost.presentation.email_verified.EmailVerifiedFragment
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.presentation.signin.SignInFragment
import com.dzyuba.javaboost.presentation.signup.SignUpFragment
import com.dzyuba.javaboost.presentation.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [ViewModelModule::class, DataModule::class])
interface ComponentApp {

    fun inject(fragment: SplashFragment)
    fun inject(fragment: ForgotPasswordFragment)
    fun inject(fragment: SignUpFragment)
    fun inject(fragment: SignInFragment)
    fun inject(fragment: EmailVerifiedFragment)
    fun inject(fragment: NicknameFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ComponentApp
    }
}