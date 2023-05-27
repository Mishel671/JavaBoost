package com.dzyuba.javaboost.data.firebase

import com.dzyuba.javaboost.domain.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

const val USERS = "users"
const val IS_ONLINE = "isOnline"
const val AVATAR = "avatar"
const val NICKNAME = "nickname"
const val LESSONS_SHORT = "lessons_short"
const val LESSONS_DETAIL = "lessons_detail"
const val RATING = "rating"
const val ALL = "all"
const val COMMENTS = "comments"

fun DatabaseReference.getUsersRow() = this.child(USERS)

fun DatabaseReference.getUser(firebaseAuth: FirebaseAuth) =
    this.child(USERS).child(firebaseAuth.currentUser!!.uid)

fun DatabaseReference.getLessons() = this.child(LESSONS_SHORT)

fun DatabaseReference.getLessonsAllRow() = this.child(LESSONS_SHORT).child(ALL)

fun DatabaseReference.getLessonDetailRow(id: Int) = this.child(LESSONS_DETAIL).child(id.toString())

fun DatabaseReference.getUserRateRow(lessonId: Int, userId: String) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(RATING).child(userId)

fun DatabaseReference.getCommentsRow(lessonId: Int) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(COMMENTS)

fun DatabaseReference.getCommentDetailRow(lessonId: Int, commentId:Long) =
    this.child(LESSONS_DETAIL).child(lessonId.toString()).child(COMMENTS).child(commentId.toString())

inline fun <reified T> DatabaseReference.listen(): Flow<Resource<T?>> =
    callbackFlow {
        val valueListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val value = dataSnapshot.getValue(T::class.java)
                    trySend(Resource.success(value))
                } catch (exp: Exception) {
                    if (!isClosedForSend) trySend(Resource.error(exp))
                }
            }
        }
        addValueEventListener(valueListener)
        awaitClose { removeEventListener(valueListener) }
    }
