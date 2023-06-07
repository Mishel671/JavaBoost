package com.dzyuba.javaboost.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

const val USERS = "users"
const val IS_ONLINE = "isOnline"
const val AVATAR = "avatar"
const val NICKNAME = "nickname"
const val LESSONS_SHORT = "lessons_short"
const val LESSONS_DETAIL = "lessons_detail"
const val RATING = "rating"
const val ALL = "all"
const val COMMENTS = "comments"
const val DECIDE_LESSONS = "decide_lessons"
const val DECIDES = "decides"
const val TASK_COUNT = "taskCount"
const val WAS_READ = "wasRead"

fun DatabaseReference.getUsersRow() = this.child(USERS)

fun DatabaseReference.getUser(firebaseAuth: FirebaseAuth) =
    this.child(USERS).child(firebaseAuth.currentUser!!.uid)

fun DatabaseReference.getLessons() = this.child(LESSONS_SHORT)

fun DatabaseReference.getLessonsAllRow() = this.child(LESSONS_SHORT).child(ALL)

fun DatabaseReference.getLessonDetailRow(id: Long) = this.child(LESSONS_DETAIL).child(id.toString())

fun DatabaseReference.getUserRateRow(lessonId: Long, userId: String) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(RATING).child(userId)

fun DatabaseReference.getCommentsRow(lessonId: Long) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(COMMENTS)

fun DatabaseReference.getCommentDetailRow(lessonId: Long, commentId: Long) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(COMMENTS)
        .child(commentId.toString())

fun DatabaseReference.getDecideTaskReadRow(userId: String, lessonId: Long) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS).child(lessonId.toString()).child(WAS_READ)

fun DatabaseReference.getDecideTaskCountRow(userId: String, lessonId: Long) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS).child(lessonId.toString()).child(TASK_COUNT)

fun DatabaseReference.getDecideLessonItemRow(userId: String, lessonId: Long, lessonItemId: Long) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS).child(lessonId.toString())
        .child(DECIDES)

fun DatabaseReference.getDecidesLessonItemRow(userId: String, lessonId: Long) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS).child(lessonId.toString())
        .child(DECIDES)

fun DatabaseReference.getDecidesLessonRow(userId: String, lessonId: Long) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS).child(lessonId.toString())

fun DatabaseReference.getDecidesLessonsRow(userId: String) =
    this.child(USERS).child(userId).child(DECIDE_LESSONS)

