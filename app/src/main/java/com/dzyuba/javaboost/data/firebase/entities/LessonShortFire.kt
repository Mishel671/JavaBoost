package com.dzyuba.javaboost.data.firebase.entities

data class LessonShortFire(
    val id: Long = -1,
    val title: String = "",
    val description: String? = null,
    val detailDescription: String? = null,
    val tags: List<String>? = null
)