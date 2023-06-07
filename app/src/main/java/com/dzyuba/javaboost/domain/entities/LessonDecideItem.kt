package com.dzyuba.javaboost.domain.entities

import com.dzyuba.javaboost.domain.entities.lesson.Type

data class LessonDecideItem(
    val id: Long,
    val type: Type,
    val wasDecided: Boolean,
    val trueAnswerId: Int? = null
)