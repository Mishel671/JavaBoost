package com.dzyuba.javaboost.data.repositories

import android.util.Log
import com.dzyuba.javaboost.data.converters.toComment
import com.dzyuba.javaboost.data.converters.toLesson
import com.dzyuba.javaboost.data.converters.toLessonDecideItem
import com.dzyuba.javaboost.data.converters.toLessonDecides
import com.dzyuba.javaboost.data.converters.toLessonShort
import com.dzyuba.javaboost.data.converters.toThrowable
import com.dzyuba.javaboost.data.firebase.DECIDES
import com.dzyuba.javaboost.data.firebase.LESSONS_DETAIL
import com.dzyuba.javaboost.data.firebase.TASK_COUNT
import com.dzyuba.javaboost.data.firebase.entities.CommentFire
import com.dzyuba.javaboost.data.firebase.entities.LessonDecideItemFire
import com.dzyuba.javaboost.data.firebase.entities.LessonDecidesFire
import com.dzyuba.javaboost.data.firebase.entities.LessonFire
import com.dzyuba.javaboost.data.firebase.entities.LessonShortFire
import com.dzyuba.javaboost.data.firebase.getCommentDetailRow
import com.dzyuba.javaboost.data.firebase.getCommentsRow
import com.dzyuba.javaboost.data.firebase.getDecideLessonItemRow
import com.dzyuba.javaboost.data.firebase.getDecidesLessonItemRow
import com.dzyuba.javaboost.data.firebase.getDecidesLessonRow
import com.dzyuba.javaboost.data.firebase.getDecidesLessonsRow
import com.dzyuba.javaboost.data.firebase.getLessonDetailRow
import com.dzyuba.javaboost.data.firebase.getLessons
import com.dzyuba.javaboost.data.firebase.getLessonsAllRow
import com.dzyuba.javaboost.data.firebase.getUserRateRow
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonDecideItem
import com.dzyuba.javaboost.domain.entities.LessonDecides
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.lesson.Comment
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.dzyuba.javaboost.domain.entities.lesson.LessonItem
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.domain.entities.lesson.Test
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

    override suspend fun getLessonsShortById(ids: List<Long>) =
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

    override suspend fun getLessonsDetail(id: Long): Resource<Lesson> =
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

    override suspend fun getDecidesList(lessonId: Long): Resource<List<LessonDecideItem>> =
        suspendCoroutine { cont ->
            firebaseDatabase.getDecidesLessonItemRow(
                firebaseAuth.currentUser!!.uid,
                lessonId
            ).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val decidesList = snapshot.children
                            .map { it.getValue(LessonDecideItemFire::class.java) }
                            .filterNotNull()
                            .map { it.toLessonDecideItem() }
                        cont.resume(Resource.success(decidesList))
                    } else {
                        cont.resume(Resource.success(listOf()))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resume(Resource.error(error.toException()))
                }
            })
        }

    override suspend fun setLessonRate(lessonId: Long, rate: Float): Resource<Unit> =
        suspendCoroutine<Resource<Unit>> { cont ->
            firebaseDatabase.getUserRateRow(lessonId, firebaseAuth.currentUser!!.uid).setValue(rate)
                .addOnSuccessListener {
                    cont.resume(Resource.success(Unit))
                }.addOnFailureListener {
                    cont.resume(Resource.error(it.toThrowable()))
                }
        }

    override suspend fun getLessonComments(lessonId: Long): Flow<Resource<List<Comment>>> =
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

    override suspend fun sendComment(lessonId: Long, comment: String): Resource<Unit> =
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

    override suspend fun setDecideTask(lessonId: Long, lessonItem: LessonItem?, taskCount: Int) {
        val changeLessonItem = if (lessonItem is Test)
            LessonDecideItemFire(
                lessonItem.id,
                "test",
                lessonItem.answerResult!! == lessonItem.trueAnswerId,
                lessonItem.answerResult
            )
        else if (lessonItem is Practice)
            LessonDecideItemFire(
                lessonItem.id,
                "practice",
                lessonItem.wasDecided!!
            ) else null
        val path = firebaseDatabase.getDecidesLessonRow(firebaseAuth.currentUser!!.uid, lessonId)
        path.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MainLog", "Snapshot task work")
                if (snapshot.exists()) {
                    val lessonDecidesFire = snapshot.getValue(LessonDecidesFire::class.java)
                    val newDecides =
                        if (changeLessonItem != null)
                            lessonDecidesFire?.decides?.apply {
                                set(
                                    changeLessonItem!!.id.toString(),
                                    changeLessonItem
                                )
                            }
                                ?: hashMapOf(changeLessonItem!!.id.toString() to changeLessonItem) else lessonDecidesFire?.decides
                    Log.d("MainLog", "Fuck: ${newDecides}")
                    snapshot.ref.setValue(lessonDecidesFire!!.copy(decides = newDecides))

                } else {
                    val decide =
                        if (changeLessonItem != null) hashMapOf(changeLessonItem!!.id.toString() to changeLessonItem) else null
                    val lessonDecidesFire = LessonDecidesFire(
                        lessonId,
                        taskCount,
                        true,
                        decide
                    )
                    path.setValue(lessonDecidesFire)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

//    override suspend fun setReadLesson(lessonId: Long, taskCount: Int) {
//        val lessonDecideFire =
//            LessonDecidesFire(id = lessonId, taskCount = taskCount, wasRead = true)
//        val path = firebaseDatabase.getDecidesLessonRow(firebaseAuth.currentUser!!.uid, lessonId)
//        path.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d("MainLog", "Snapshot read work")
//                if (snapshot.exists()) {
//                    val lessonDecides = snapshot.getValue(LessonDecidesFire::class.java)
//                    path.setValue(lessonDecideFire.copy(decides = lessonDecides?.decides))
//                } else {
//                    path.setValue(lessonDecideFire)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })
////        firebaseDatabase.getDecideTaskReadRow(firebaseAuth.currentUser!!.uid, lessonId.toLong())
////            .setValue(true)
////        firebaseDatabase.getDecideTaskCountRow(firebaseAuth.currentUser!!.uid, lessonId.toLong())
////            .setValue(taskCount)
//    }

    override suspend fun getLessonsDecide(): Resource<List<LessonDecides>> =
        suspendCoroutine { cont ->
            firebaseDatabase.getDecidesLessonsRow(firebaseAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val list = snapshot.children
                                    .map { it.getValue(LessonDecidesFire::class.java) }
                                    .filterNotNull()
                                    .map { it.toLessonDecides() }
                                cont.resume(Resource.success(list))
                            } else {
                                cont.resume(Resource.success(listOf()))
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            cont.resume(Resource.error(error.toException()))
                        }

                    }
                )
        }

    override suspend fun getAllTaskCount(): Resource<List<Int>> = suspendCoroutine { cont ->
        firebaseDatabase.child(LESSONS_DETAIL).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val list = snapshot.children
                            .map { it.child(TASK_COUNT).getValue(Int::class.java) }
                            .filterNotNull()
                        cont.resume(Resource.success(list))
                    }else{
                        cont.resume(Resource.success(listOf()))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resume(Resource.error(error.toException()))
                }
            }
        )
    }
}