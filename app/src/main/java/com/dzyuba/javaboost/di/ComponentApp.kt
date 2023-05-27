package com.dzyuba.javaboost.di

import android.app.Application
import com.dzyuba.javaboost.presentation.comments.CommentsFragment
import com.dzyuba.javaboost.presentation.email_verified.EmailVerifiedFragment
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordFragment
import com.dzyuba.javaboost.presentation.lesson_detail.LessonDetailFragment
import com.dzyuba.javaboost.presentation.lessons.LessonsListFragment
import com.dzyuba.javaboost.presentation.main.MainFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.presentation.profile.ProfileFragment
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
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: MainFragment)
    fun inject(fragment: LessonsListFragment)
    fun inject(fragment: LessonDetailFragment)
    fun inject(fragment: CommentsFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ComponentApp
    }
}