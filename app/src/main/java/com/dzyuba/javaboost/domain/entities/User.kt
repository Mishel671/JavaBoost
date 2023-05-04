package com.dzyuba.javaboost.domain.entities

import android.net.Uri

data class User(
    val userUid: String,
    val name: String?,
    val email: String,
    val photoUrl: Uri?,
    val isEmailVerified: Boolean
)
