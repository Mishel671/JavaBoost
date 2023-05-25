package com.dzyuba.javaboost.data.repositories

import android.graphics.Bitmap
import com.dzyuba.javaboost.data.firebase.*
import com.dzyuba.javaboost.data.converters.toLessonShort
import com.dzyuba.javaboost.data.converters.toThrowable
import com.dzyuba.javaboost.data.converters.toUser
import com.dzyuba.javaboost.data.firebase.entities.LessonShortFire
import com.dzyuba.javaboost.data.firebase.entities.UserFire
import com.dzyuba.javaboost.domain.ProfileRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonShort
import com.dzyuba.javaboost.domain.entities.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: DatabaseReference,
    private val firebaseStorage: StorageReference,
) : ProfileRepository {


    override fun isAuthenticated() = firebaseAuth.currentUser != null

    override fun getUser() = firebaseAuth.currentUser!!.toUser()

    override suspend fun loadUser(): Resource<User> = suspendCoroutine { cont ->
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser!!.reload().addOnCompleteListener { task ->
                if (task.isSuccessful)
                    cont.resume(Resource.success(firebaseAuth.currentUser!!.toUser()))
                else
                    cont.resume(Resource.error(task.exception.toThrowable()))
            }
        } else {
            cont.resume(Resource.error(Throwable("User not registered")))
        }
    }

    override suspend fun registration(email: String, password: String) =
        suspendCoroutine<Resource<Unit>> { cont ->
            firebaseAuth.createUserWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    cont.resume(Resource.success(Unit))
                else
                    cont.resume(Resource.error(task.exception.toThrowable()))
            }
        }

    override suspend fun signIn(email: String, password: String) =
        suspendCoroutine<Resource<User>> { cont ->
            firebaseAuth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    cont.resume(Resource.success(task.result.user!!.toUser()))
                else
                    cont.resume(Resource.error(task.exception.toThrowable()))
            }
        }

    override fun logout() = firebaseAuth.signOut()


    override suspend fun verificationEmail(): Resource<Unit> = suspendCoroutine { cont ->
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser!!.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        cont.resume(Resource.success(Unit))
                    else
                        cont.resume(Resource.error(task.exception.toThrowable()))
                }

        } else {
            cont.resume(Resource.error(Throwable("User not registered")))
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> = suspendCoroutine { cont ->
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful)
                cont.resume(Resource.success(Unit))
            else
                cont.resume(Resource.error(task.exception.toThrowable()))
        }
    }

    override suspend fun updateProfileName(name: String): Resource<Unit> =
        suspendCoroutine { cont ->
            if (firebaseAuth.currentUser != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                firebaseAuth.currentUser!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            firebaseDatabase.child(USERS).child(firebaseAuth.currentUser!!.uid)
                                .child(NICKNAME).setValue(name).addOnCompleteListener { realTask ->
                                    cont.resume(
                                        if (realTask.isSuccessful) Resource.success(Unit)
                                        else Resource.error(realTask.exception.toThrowable())
                                    )
                                }
                        else
                            cont.resume(Resource.error(task.exception.toThrowable()))
                    }
            } else {
                cont.resume(Resource.error(Throwable("User not registered")))
            }
        }

    override suspend fun updateProfileImage(image: Bitmap): Resource<Unit> =
        suspendCoroutine { cont ->
            val imagesRef = firebaseStorage.child(
                "images/" + firebaseAuth.currentUser?.uid + ".jpg"
            )
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data: ByteArray = baos.toByteArray()
            imagesRef.putBytes(data).addOnCompleteListener { loadTask ->
                if (loadTask.isSuccessful)
                    imagesRef.downloadUrl.addOnCompleteListener { uriTask ->
                        if (uriTask.isSuccessful) {
                            if (firebaseAuth.currentUser != null) {
                                val profileUpdates = userProfileChangeRequest {
                                    photoUri = uriTask.result
                                }
                                firebaseAuth.currentUser!!.updateProfile(profileUpdates)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful)
                                            firebaseDatabase.child(USERS)
                                                .child(firebaseAuth.currentUser!!.uid)
                                                .child(AVATAR)
                                                .setValue(firebaseAuth.currentUser!!.photoUrl?.toString())
                                                .addOnCompleteListener { task ->
                                                    cont.resume(
                                                        if (task.isSuccessful)
                                                            Resource.success(Unit)
                                                        else Resource.error(task.exception.toThrowable())
                                                    )
                                                }
                                        else
                                            cont.resume(Resource.error(uriTask.exception.toThrowable()))
                                    }
                            } else
                                cont.resume(Resource.error(Throwable("User not registered")))
                        } else
                            cont.resume(Resource.error(uriTask.exception.toThrowable()))
                    }
                else
                    cont.resume(Resource.error(loadTask.exception.toThrowable()))
            }
        }


    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> =
        suspendCoroutine { cont ->
            firebaseAuth.currentUser?.let {
                val credential = EmailAuthProvider.getCredential(it.email!!, oldPassword)
                it.reauthenticate(credential).addOnCompleteListener { reauthenticateTask ->
                    if (reauthenticateTask.isSuccessful)
                        it.updatePassword(newPassword).addOnCompleteListener { passwordTask ->
                            if (passwordTask.isSuccessful)
                                cont.resume(Resource.success(Unit))
                            else
                                cont.resume(Resource.error(passwordTask.exception.toThrowable()))
                        }
                    else
                        cont.resume(Resource.error(reauthenticateTask.exception.toThrowable()))
                }
            }
            cont.resume(Resource.error(Throwable("User not registered")))
        }

    override suspend fun updateConnection() {
        val usersRef = firebaseDatabase.child(USERS)
        val userRef = usersRef.child(firebaseAuth.currentUser!!.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userRef.child(IS_ONLINE).apply {
                        setValue(true)
                        onDisconnect().setValue(false)
                    }
                } else {
                    val userFire = UserFire(
                        id = firebaseAuth.currentUser!!.uid,
                        avatar = firebaseAuth.currentUser?.photoUrl?.toString(),
                        nickname = firebaseAuth.currentUser!!.displayName!!,
                        isOnline = true,
                        lastLesson = null,
                        learnedLessons = null
                    )
                    usersRef.setValue(userFire).addOnCompleteListener {
                        if (it.isSuccessful) {
                            usersRef.child(userFire.id).child(IS_ONLINE).onDisconnect()
                                .setValue(false)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}