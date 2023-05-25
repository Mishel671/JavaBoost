package com.dzyuba.javaboost.domain.entities.lesson

data class Practice(
    override val id: Int,
    override val type: Type = Type.PRACTICE,
    val task: String,
    val inputFormat: String,
    val resultFormat: String,
    val keyWords: List<String>?,
    val wasDecided: Boolean? = null
) : LessonItem(id, type)