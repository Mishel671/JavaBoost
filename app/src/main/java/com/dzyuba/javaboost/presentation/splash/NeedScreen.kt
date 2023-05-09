package com.dzyuba.javaboost.presentation.splash

enum class NeedScreen(var error: Throwable? = null) {
    SIGN_IN,
    EMAIL_VERIFIED,
    NICKNAME,
    MAIN_SCREEN,
    ERROR
}