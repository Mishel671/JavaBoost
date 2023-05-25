package com.dzyuba.javaboost.presentation.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.domain.ProfileRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.util.isEmailValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _registration = MutableLiveData<Resource<Unit>>()
    val registration: LiveData<Resource<Unit>>
        get() = _registration

    private val _inputErrors = MutableLiveData<SignUpInputErrors>(SignUpInputErrors())
    val inputErrors: LiveData<SignUpInputErrors>
        get() = _inputErrors

    fun singUp(inputEmail: String?, inputPassword: String?, inputRepeatPassword: String?) {
        val email = inputEmail?.trim() ?: ""
        val password = inputPassword?.trim() ?: ""
        val repeatPassword = inputRepeatPassword?.trim() ?: ""
        val inputValues = checkInputValues(email, password, repeatPassword)
        if (inputValues) {
            _registration.value = Resource.loading()
            viewModelScope.launch(Dispatchers.IO) {
                val result = repository.registration(email, password)
                    .ifSuccess { repository.logout() }
                _registration.postValue(result)
            }
        }
    }

    private fun checkInputValues(email: String, password: String, repeatPassword: String): Boolean {
        val errors = SignUpInputErrors()
        var result = true
        //Email
        if (email.isEmpty()) {
            result = false
            errors.errorEmail = R.string.sign_up_input_empty
        } else if (!email.isEmailValid()) {
            result = false
            errors.errorEmail = R.string.sign_up_input_email_error
        }
        //Password
        if (password.isEmpty()) {
            result = false
            errors.errorPassword = R.string.sign_up_input_empty
        } else if (password.length < 8) {
            result = false
            errors.errorPassword = R.string.sign_up_input_password_error
        }
        //Repeat password
        if (repeatPassword.isEmpty()) {
            result = false
            errors.errorRepeatPassword = R.string.sign_up_input_empty
        } else if (repeatPassword != password) {
            result = false
            errors.errorRepeatPassword = R.string.sign_up_input_repeat_password_error
        }
        if (!result) {
            _inputErrors.value = errors
        }
        return result
    }

    fun resetEmail() {
        _inputErrors.value = _inputErrors.value?.copy(errorEmail = null)
    }

    fun resetPassword() {
        _inputErrors.value = _inputErrors.value?.copy(errorPassword = null)
    }

    fun resetRepeatPassword() {
        _inputErrors.value = _inputErrors.value?.copy(errorRepeatPassword = null)
    }
}