package com.dzyuba.javaboost.domain.entities.lesson

data class Code(
    override val id: Long,
    override val type: Type = Type.CODE,
    val text: String,
    val description:String?
) : LessonItem(id,type)