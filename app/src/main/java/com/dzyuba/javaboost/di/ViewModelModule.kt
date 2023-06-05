package com.dzyuba.javaboost.di

import androidx.lifecycle.ViewModel
import com.dzyuba.javaboost.presentation.comments.CommentsViewModel
import com.dzyuba.javaboost.presentation.email_verified.EmailVerifiedViewModel
import com.dzyuba.javaboost.presentation.forgot_password.ForgotPasswordViewModel
import com.dzyuba.javaboost.presentation.ide.console.ConsoleViewModel
import com.dzyuba.javaboost.presentation.ide.editor.EditorViewModel
import com.dzyuba.javaboost.presentation.lesson_detail.LessonDetailViewModel
import com.dzyuba.javaboost.presentation.lessons.LessonsListViewModel
import com.dzyuba.javaboost.presentation.main.MainViewModel
import com.dzyuba.javaboost.presentation.nickname.NicknameViewModel
import com.dzyuba.javaboost.presentation.profile.ProfileViewModel
import com.dzyuba.javaboost.presentation.signin.SignInViewModel
import com.dzyuba.javaboost.presentation.signup.SignUpViewModel
import com.dzyuba.javaboost.presentation.splash.SplashViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun bindSignInViewModel(viewModel: SignInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel::class)
    fun bindForgotPasswordViewModel(viewModel: ForgotPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    fun bindSignUpViewModel(viewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EmailVerifiedViewModel::class)
    fun bindEmailVerifiedViewModel(viewModel: EmailVerifiedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NicknameViewModel::class)
    fun bindNicknameViewModel(viewModel: NicknameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    fun bindProfileViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LessonsListViewModel::class)
    fun bindLessonsListViewModel(viewModel: LessonsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LessonDetailViewModel::class)
    fun bindLessonDetailViewModel(viewModel: LessonDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    fun bindCommentsViewModel(viewModel: CommentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditorViewModel::class)
    fun bindEditorViewModel(viewModel: EditorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConsoleViewModel::class)
    fun bindConsoleViewModel(viewModel: ConsoleViewModel): ViewModel
}