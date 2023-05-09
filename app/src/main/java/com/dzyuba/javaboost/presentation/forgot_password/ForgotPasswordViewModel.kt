package com.dzyuba.javaboost.presentation.forgot_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.FirebaseRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.util.isEmailValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _resetPassword = MutableLiveData<Resource<Unit>>()
    val resetPassword: LiveData<Resource<Unit>>
        get() = _resetPassword

    private val _inputError = MutableLiveData<Boolean>()
    val inputError: LiveData<Boolean>
        get() = _inputError

    fun sendPasswordByEmail(inputEmail: String?) {
        val email = inputEmail?.trim() ?: ""
        if (email.isEmailValid()) {
            _resetPassword.value = Resource.loading()
            viewModelScope.launch(Dispatchers.IO) {
                _resetPassword.postValue(firebaseRepository.resetPassword(email))
            }
        } else {
            _inputError.value = true
        }
    }

    fun resetError() {
        _inputError.value = false
    }
}