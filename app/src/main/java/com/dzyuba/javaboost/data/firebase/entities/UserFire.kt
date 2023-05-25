package com.dzyuba.javaboost.data.firebase.entities

data class UserFire(
    val id: String,
    val avatar: String?,
    val nickname: String,
    val isOnline: Boolean,
    val lastLesson: Int?,
    val learnedLessons:List<Int>?
    )