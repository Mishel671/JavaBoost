package com.dzyuba.javaboost.domain.entities

data class LessonDecides(
    val id: Long,
    val taskCount: Int,
    val wasRead: Boolean,
    val decides: List<LessonDecideItem>
)