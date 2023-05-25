package com.dzyuba.javaboost.domain.entities.lesson

data class Test(
    override val id: Int,
    override val type: Type = Type.TEST,
    val question: String,
    val answers: HashSet<Answer>,
    val trueAnswerId: Int,
    var answerResult: Boolean? = null
) : LessonItem(id, type) {
    data class Answer(
        val id: Int,
        val answer: String
    )
}