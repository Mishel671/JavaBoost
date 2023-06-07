package com.dzyuba.javaboost.data.firebase.entities

import com.dzyuba.javaboost.domain.entities.lesson.Test

data class LessonItemFire(
    val id: Long = -1,
    val type: String? = null,
    //Header, text, code
    val text: String? = null,
    //Image, code
    val description:String? = null,
    //Image
    val imageUrl: String? = null,

    //Divider
    val height: Int? = null,

    //Test
    val question: String? = null,
    val answers: List<AnswerFire>? = null,
    val trueAnswerId: Int? = null,

    //Practice
    val task: String? = null,
    val inputFormat: String? = null,
    val resultFormat: String? = null,
    val codeKeyWords: List<String>? = null,
    val outputKeyWords: List<String>? = null,
    val wasDecided: Boolean? = null
)