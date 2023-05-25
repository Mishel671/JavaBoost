package com.dzyuba.javaboost.presentation.email_verified

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.ProfileRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmailVerifiedViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _verificationEmail = MutableLiveData<Resource<Unit>>()
    val verificationEmail: LiveData<Resource<Unit>>
        get() = _verificationEmail

    private val _emailIsVerified = MutableLiveData<Resource<User>>()
    val emailIsVerified: LiveData<Resource<User>>
        get() = _emailIsVerified

    val user = repository.getUser()

    fun checkIsVerified() {
        _emailIsVerified.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            _emailIsVerified.postValue(repository.loadUser())
        }
    }

    fun sendVerifyEmail() {
        _verificationEmail.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            _verificationEmail.postValue(repository.verificationEmail())
        }
    }

    fun logout() {
        repository.logout()
    }
}