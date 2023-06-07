package com.dzyuba.javaboost.data.converters

import com.dzyuba.javaboost.data.firebase.entities.*
import com.dzyuba.javaboost.domain.entities.LessonDecideItem
import com.dzyuba.javaboost.domain.entities.LessonDecides
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.LessonShort.Tags.Companion.toTag
import com.dzyuba.javaboost.domain.entities.User
import com.dzyuba.javaboost.domain.entities.lesson.*
import com.dzyuba.javaboost.domain.entities.lesson.Type.Companion.toType
import com.google.firebase.auth.FirebaseUser

fun Exception?.toThrowable() = Throwable(this?.message ?: "Unknown error")

fun FirebaseUser.toUser() = User(
    userUid = uid,
    name = displayName,
    email = email!!,
    photoUrl = photoUrl,
    isEmailVerified = isEmailVerified
)

fun LessonShortFire.toLessonShort() = LessonShort(
    id = id,
    title = title,
    description = description ?: "",
    detailDescription = detailDescription ?: "",
    tags = tags?.map { it.toTag() }?.filterNotNull() ?: listOf()
)

fun LessonFire.toLesson() = Lesson(
    id = id,
    lessonName = lessonName!!,
    rating = rating,
//    comments = comments?.map { it.toComment() },
    lessonItems = lessonItems!!.toLessonItemList()
)

fun CommentFire.toComment() = Comment(
    id = id,
    userId = userId!!,
    userName = userName!!,
    userLogo = userLogo,
    text = text!!
)

private fun List<LessonItemFire>.toLessonItemList() = map { it.toLessonItem() }.filterNotNull()

private fun LessonItemFire.toLessonItem() = when (type?.toType()) {
    Type.HEADER -> Header(id = id, text = text!!)
    Type.TEXT -> Text(id = id, text = text!!)
    Type.IMAGE -> Image(id = id, imageUrl = imageUrl!!, description = description)
    Type.CODE -> Code(id = id, text = text!!, description = description)
    Type.DIVIDER -> Divider(id = id, heightInDp = height ?: 10)
    Type.TEST -> {
        Test(
            id = id,
            question = question!!,
            answers = answers!!.map { it.toAnswer() },
            trueAnswerId = trueAnswerId!!
        )
    }

    Type.PRACTICE -> {
        Practice(
            id = id,
            task = task!!,
            inputFormat = inputFormat,
            outputFormat = resultFormat,
            codeKeyWords = codeKeyWords,
            outputKeyWords = outputKeyWords!!,
            wasDecided = wasDecided
        )
    }

    null -> null
}


private fun AnswerFire.toAnswer() =
    Test.Answer(
        id = id,
        answer = answer
    )

fun LessonDecidesFire.toLessonDecides() = LessonDecides(
    id = id,
    taskCount = taskCount ?: 0,
    wasRead = wasRead!!,
    decides = decides?.values?.toList()?.map { it.toLessonDecideItem() }?: listOf()
//    decides = decides?.map { it.toLessonDecideItem() } ?: listOf()
)

fun LessonDecideItemFire.toLessonDecideItem() = LessonDecideItem(
    id = id,
    type = type!!.toType()!!,
    wasDecided = wasDecided ?: false,
    trueAnswerId = answerId
)

