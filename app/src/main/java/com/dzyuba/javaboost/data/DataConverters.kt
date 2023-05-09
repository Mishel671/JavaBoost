package com.dzyuba.javaboost.data

import com.dzyuba.javaboost.domain.entities.User
import com.google.firebase.auth.FirebaseUser

fun Exception?.toThrowable() = Throwable(this?.message ?: "Unknown error")

fun FirebaseUser.toUser() = User(
    userUid = uid,
    name = displayName,
    email = email!!,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified
)