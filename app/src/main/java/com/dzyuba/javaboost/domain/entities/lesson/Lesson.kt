package com.dzyuba.javaboost.domain.entities.lesson

data class Lesson(
    val id: Long,
    val lessonName:String,
    val rating: HashMap<String, Float>?,
//    val comments: List<Comment>?,
    val lessonItems: List<LessonItem>
)