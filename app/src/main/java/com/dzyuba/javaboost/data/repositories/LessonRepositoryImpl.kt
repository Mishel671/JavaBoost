package com.dzyuba.javaboost.data.repositories

import com.dzyuba.javaboost.data.converters.toComment
import com.dzyuba.javaboost.data.converters.toLesson
import com.dzyuba.javaboost.data.converters.toLessonShort
import com.dzyuba.javaboost.data.converters.toThrowable
import com.dzyuba.javaboost.data.firebase.entities.CommentFire
import com.dzyuba.javaboost.data.firebase.entities.LessonFire
import com.dzyuba.javaboost.data.firebase.entities.LessonShortFire
import com.dzyuba.javaboost.data.firebase.getCommentDetailRow
import com.dzyuba.javaboost.data.firebase.getCommentsRow
import com.dzyuba.javaboost.data.firebase.getLessonDetailRow
import com.dzyuba.javaboost.data.firebase.getLessonsAllRow
import com.dzyuba.javaboost.data.firebase.getUserRateRow
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.lesson.Comment
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LessonRepositoryImpl @Inject constructor(
    private val firebaseDatabase: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : LessonRepository {

    private var nextCommentId = 0L

    override fun getUserId() = firebaseAuth.currentUser!!.uid

    override suspend fun getLessonsShort() = suspendCoroutine<Resource<List<LessonShort>>> { cont ->
        firebaseDatabase.getLessonsAllRow()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val list = snapshot.children
                            .map {
                                it.getValue(LessonShortFire::class.java)
                            }
                            .filterNotNull()
                            .map { it.toLessonShort() }
                        cont.resume(Resource.success(list))
                    } else {
                        cont.resume(Resource.error(Throwable("Data not exist")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resume(Resource.error(error.toException().toThrowable()))
                }
            })
    }

    override suspend fun getLessonsShortById(ids: List<Int>) =
        suspendCoroutine<Resource<List<LessonShort>>> { cont ->
            firebaseDatabase.getLessonsAllRow()
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val list = snapshot.children
                                .map { it.getValue(LessonShortFire::class.java) }
                                .filterNotNull()
                                .filter { ids.contains(it.id) }
                                .map { it.toLessonShort() }
                            cont.resume(Resource.success(list))
                        } else {
                            cont.resume(Resource.error(Throwable("Data not exist")))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cont.resume(Resource.error(error.toException().toThrowable()))
                    }
                })
        }

    override suspend fun getLessonsDetail(id: Int) =
        suspendCoroutine<Resource<Lesson>> { cont ->
            firebaseDatabase.getLessonDetailRow(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val lessonFire = snapshot.getValue(LessonFire::class.java)
                            if (lessonFire != null && lessonFire.lessonItems?.isNotEmpty() == true) {
                                val lesson = lessonFire.toLesson()
                                if (lesson.lessonItems.isNotEmpty())
                                    cont.resume(Resource.success(lesson))
                                else
                                    cont.resume(Resource.error(Throwable("Lesson format is invalid")))
                            } else
                                cont.resume(Resource.error(Throwable("Lesson loading error")))
                        } else
                            cont.resume(Resource.error(Throwable("Data not exist")))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cont.resume(Resource.error(error.toException().toThrowable()))
                    }

                })
        }

    override suspend fun setLessonRate(lessonId: Int, rate: Float) =
        suspendCoroutine<Resource<Unit>> { cont ->
            firebaseDatabase.getUserRateRow(lessonId, firebaseAuth.currentUser!!.uid).setValue(rate)
                .addOnSuccessListener {
                    cont.resume(Resource.success(Unit))
                }.addOnFailureListener {
                    cont.resume(Resource.error(it.toThrowable()))
                }
        }

    override suspend fun getLessonComments(lessonId: Int): Flow<Resource<List<Comment>>> =
        callbackFlow {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    nextCommentId = snapshot.childrenCount
                    if (snapshot.exists()) {
                        val comments = snapshot.children.map {
                            it.getValue(CommentFire::class.java)
                        }.filterNotNull().map { it.toComment() }.reversed()
                        trySend(Resource.success(comments))
                    } else {
                        trySend(Resource.success(listOf()))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.error(error.toException()))
                }

            }
            firebaseDatabase.getCommentsRow(lessonId).addValueEventListener(valueEventListener)
            awaitClose {
                nextCommentId = 0
                firebaseDatabase.removeEventListener(valueEventListener)
            }
        }

    override suspend fun sendComment(lessonId: Int, comment: String) =
        suspendCoroutine<Resource<Unit>> { count ->
            val commentFire = CommentFire(
                id = nextCommentId,
                userId = firebaseAuth.currentUser!!.uid,
                userName = firebaseAuth.currentUser!!.displayName,
                userLogo = firebaseAuth.currentUser!!.photoUrl?.toString(),
                text = comment
            )
            firebaseDatabase.getCommentDetailRow(lessonId, commentFire.id).setValue(commentFire)
                .addOnSuccessListener {
                    count.resume(Resource.success(Unit))
                }.addOnFailureListener {
                    count.resume(Resource.error(it.toThrowable()))
                }
        }
}