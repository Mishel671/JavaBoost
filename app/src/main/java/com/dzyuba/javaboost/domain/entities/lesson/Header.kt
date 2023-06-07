package com.dzyuba.javaboost.domain.entities.lesson

data class Header(
    override val id: Long,
    override val type: Type = Type.HEADER,
    val text: String
) : LessonItem(id,type)