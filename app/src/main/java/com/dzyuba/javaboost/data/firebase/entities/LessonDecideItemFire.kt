package com.dzyuba.javaboost.data.firebase.entities

data class LessonDecideItemFire(
    val id: Long = -1,
    val type: String? = null,
    val wasDecided: Boolean? = null,
    val answerId: Int? = null
)