package com.dzyuba.javaboost.domain.entities.lesson

import java.io.Serializable

data class Practice(
    override val id: Long,
    override val type: Type = Type.PRACTICE,
    val task: String,
    val inputFormat: String?,
    val outputFormat: String?,
    val codeKeyWords: List<String>?,
    val outputKeyWords: List<String>?,
    val wasDecided: Boolean? = null
) : LessonItem(id, type), Serializable