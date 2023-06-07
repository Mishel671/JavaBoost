package com.dzyuba.javaboost.domain

import com.dzyuba.javaboost.domain.entities.LessonDecideItem
import com.dzyuba.javaboost.domain.entities.LessonDecides
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.lesson.Comment
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.dzyuba.javaboost.domain.entities.lesson.LessonItem
import kotlinx.coroutines.flow.Flow

interface LessonRepository {

    fun getUserId(): String

    suspend fun getLessonsShort(): Resource<List<LessonShort>>

    suspend fun getLessonsShortById(ids: List<Long>): Resource<List<LessonShort>>

    suspend fun getLessonsDetail(id: Long): Resource<Lesson>

    suspend fun setLessonRate(lessonId: Long, rate: Float): Resource<Unit>

    suspend fun getLessonComments(lessonId: Long): Flow<Resource<List<Comment>>>

    suspend fun sendComment(lessonId: Long, comment: String): Resource<Unit>

    suspend fun setDecideTask(lessonId: Long,lessonItem: LessonItem?, taskCount:Int)

    suspend fun getDecidesList(lessonId: Long): Resource<List<LessonDecideItem>>
    suspend fun getLessonsDecide(): Resource<List<LessonDecides>>

    suspend fun getAllTaskCount(): Resource<List<Int>>
}