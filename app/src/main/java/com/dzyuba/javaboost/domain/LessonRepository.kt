package com.dzyuba.javaboost.domain

import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.lesson.Lesson

interface LessonRepository {

    fun getUserId(): String

    suspend fun getLessonsShort(): Resource<List<LessonShort>>

    suspend fun getLessonsShortById(ids: List<Int>): Resource<List<LessonShort>>

    suspend fun getLessonsDetail(id: Int): Resource<Lesson>

    suspend fun setLessonRate(lessonId: Int, rate:Float): Resource<Unit>
}