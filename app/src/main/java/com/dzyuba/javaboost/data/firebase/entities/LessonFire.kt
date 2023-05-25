package com.dzyuba.javaboost.data.firebase.entities

data class LessonFire(
    val id: Int = -1,
    val lessonName:String? = null,
    val rating: HashMap<String, Float>? = null,
    val comments:List<CommentFire>? = null,
    val lessonItems: List<LessonItemFire>? = null
)