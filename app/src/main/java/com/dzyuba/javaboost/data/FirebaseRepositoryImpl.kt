package com.dzyuba.javaboost.data

import android.graphics.Bitmap
import com.dzyuba.javaboost.domain.FirebaseRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.User
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FirebaseRepositoryImpl @Inject constructor(

) : FirebaseRepository {

    private val firebaseAuth by lazy {
        Firebase.auth
    }

    private val firebaseStorage by lazy {
        Firebase.storage
    }

    private val _userFlow = MutableStateFlow<User?>(null)
    override val userFlow = _userFlow.asStateFlow()
    private var authJob: Job? = null

    private val authListener = AuthStateListener { auth ->
        authJob?.cancel()
        authJob = CoroutineScope(Dispatchers.IO).launch {
            _userFlow.emit(auth.currentUser?.toUser())
        }
    }

    override fun isAuthenticated() = firebaseAuth.currentUser != null

    override fun getUser() = firebaseAuth.currentUser!!.toUser()

    override suspend fun loadUser(): Resource<User> = suspendCoroutine { cont ->
        firebaseAuth.currentUser?.let {
            it.reload().addOnCompleteListener { task ->
                if (task.isSuccessful)
                    cont.resume(Resource.success(firebaseAuth.currentUser!!.toUser()))
                else
                    cont.resume(Resource.error(task.exception.toThrowable()))
            }
        }
        cont.resume(Resource.error(Throwable("User not registered")))
    }


    override fun subscribeFirebaseUserChanged() {
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun unsubscribeFirebaseUserChanged() {
        firebaseAuth.removeAuthStateListener(authListener)
        authJob?.cancel()
        authJob = null
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
        suspendCoroutine<Resource<Unit>> { cont ->
            firebaseAuth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    cont.resume(Resource.success(Unit))
                else
                    cont.resume(Resource.error(task.exception.toThrowable()))
            }
        }


    override suspend fun verificationEmail(): Resource<Unit> = suspendCoroutine { cont ->
        firebaseAuth.currentUser?.let {
            it.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        cont.resume(Resource.success(Unit))
                    else
                        cont.resume(Resource.error(task.exception.toThrowable()))
                }
        }
        cont.resume(Resource.error(Throwable("User not registered")))
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
            firebaseAuth.currentUser?.let {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        cont.resume(Resource.success(Unit))
                    else
                        cont.resume(Resource.error(task.exception.toThrowable()))

                }
            }
            cont.resume(Resource.error(Throwable("User not registered")))
        }

    override suspend fun updateProfileImage(image: Bitmap): Resource<Unit> =
        suspendCoroutine { cont ->
            val imagesRef = firebaseStorage.reference.child(
                "images/" + firebaseAuth.currentUser?.uid + ".jpg"
            )
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data: ByteArray = baos.toByteArray()
            imagesRef.putBytes(data).addOnCompleteListener { loadTask ->
                if (loadTask.isSuccessful)
                    imagesRef.downloadUrl.addOnCompleteListener { uriTask ->
                        if (uriTask.isSuccessful) {
                            firebaseAuth.currentUser?.let {
                                val profileUpdates = userProfileChangeRequest {
                                    photoUri = uriTask.result
                                }
                                it.updateProfile(profileUpdates)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful)
                                            cont.resume(Resource.success(Unit))
                                        else
                                            cont.resume(Resource.error(uriTask.exception.toThrowable()))
                                    }
                            }
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
}