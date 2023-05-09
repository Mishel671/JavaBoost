package com.dzyuba.javaboost.presentation.signin

import androidx.annotation.StringRes

data class SignInInputErrors(
    @StringRes
    var errorEmail: Int? = null,
    @StringRes
    var errorPassword: Int? = null
)