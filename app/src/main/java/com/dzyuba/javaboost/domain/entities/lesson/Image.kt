package com.dzyuba.javaboost.domain.entities.lesson

data class Image(
    override val id: Long,
    override val type: Type = Type.IMAGE,
    val imageUrl: String,
    val description: String?
) : LessonItem(id, type)