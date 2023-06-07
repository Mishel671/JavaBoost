package com.dzyuba.javaboost.data.firebase.entities

data class LessonDecidesFire(
    val id: Long = -1,
    val taskCount: Int? = null,
    val wasRead: Boolean? = null,
    val decides: HashMap<String,LessonDecideItemFire>? = null
)