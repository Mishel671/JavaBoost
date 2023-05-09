package com.dzyuba.javaboost.presentation.signup

import androidx.annotation.StringRes

data class SignUpInputErrors(
    @StringRes
    var errorEmail: Int? = null,
    @StringRes
    var errorPassword: Int? = null,
    @StringRes
    var errorRepeatPassword: Int? = null
)