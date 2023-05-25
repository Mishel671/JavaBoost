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

fun DatabaseReference.getUsersRow() = this.child(USERS)

fun DatabaseReference.getUser(firebaseAuth: FirebaseAuth) =
    this.child(USERS).child(firebaseAuth.currentUser!!.uid)

fun DatabaseReference.getLessons() = this.child(LESSONS_SHORT)

fun DatabaseReference.getLessonsAllRow() = this.child(LESSONS_SHORT).child(ALL)

fun DatabaseReference.getLessonDetailRow(id: Int) = this.child(LESSONS_DETAIL).child(id.toString())

fun DatabaseReference.getUserRateRow(lessonId: Int, userId: String) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(RATING).child(userId)