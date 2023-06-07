package com.dzyuba.javaboost.domain.entities.lesson

data class Divider(
    override val id: Long,
    override val type: Type = Type.DIVIDER,
    val heightInDp: Int
) : LessonItem(id, type)