package com.dzyuba.javaboost.domain.entities.lesson

data class Test(
    override val id: Long,
    override val type: Type = Type.TEST,
    val question: String,
    val answers: List<Answer>,
    val trueAnswerId: Int,
    val answerResult: Int? = null
) : LessonItem(id, type) {
    data class Answer(
        val id: Int,
        val answer: String
    )
}