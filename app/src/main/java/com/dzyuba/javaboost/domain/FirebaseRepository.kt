package com.dzyuba.javaboost.domain

import android.graphics.Bitmap
import com.dzyuba.javaboost.domain.entities.User
import kotlinx.coroutines.flow.StateFlow

interface FirebaseRepository {

    fun isAuthenticated(): Boolean

    fun getUser(): User

    suspend fun loadUser(): Resource<User>

    suspend fun registration(email: String, password: String): Resource<Unit>

    fun logout()

    suspend fun signIn(email: String, password: String): Resource<User>

    suspend fun verificationEmail(): Resource<Unit>

    suspend fun resetPassword(email: String): Resource<Unit>

    suspend fun updateProfileName(name: String): Resource<Unit>

    suspend fun updateProfileImage(image: Bitmap): Resource<Unit>

    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>
}