package com.dzyuba.javaboost.domain.entities.lesson

data class Text(
    override val id: Long,
    override val type: Type = Type.TEXT,
    val text: String
) : LessonItem(id, type)