package com.dzyuba.javaboost.presentation.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.domain.FirebaseRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.User
import com.dzyuba.javaboost.util.Event
import com.dzyuba.javaboost.util.isEmailValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _signIn = MutableLiveData<Event<Resource<User>>>()
    val signIn: LiveData<Event<Resource<User>>>
        get() = _signIn

    private val _inputErrors = MutableLiveData<SignInInputErrors>(SignInInputErrors())
    val inputErrors: LiveData<SignInInputErrors>
        get() = _inputErrors

    fun isAuthenticated() = repository.isAuthenticated()

    fun signIn(inputEmail: String?, inputPassword: String?) {
        val email = inputEmail?.trim() ?: ""
        val password = inputPassword?.trim() ?: ""
        val inputValues = checkInputValues(email, password)
        if (inputValues) {
            _signIn.value = Event(Resource.loading())
            viewModelScope.launch(Dispatchers.IO) {
                _signIn.postValue(Event(repository.signIn(email, password)))
            }
        }
    }


    private fun checkInputValues(email: String, password: String): Boolean {
        val errors = SignInInputErrors()
        var result = true
        //Email
        if (email.isEmpty()) {
            result = false
            errors.errorEmail = R.string.sign_in_input_empty
        } else if (!email.isEmailValid()) {
            result = false
            errors.errorEmail = R.string.sign_in_input_email_error
        }
        //Password
        if (password.isEmpty()) {
            result = false
            errors.errorPassword = R.string.sign_in_input_empty
        } else if (password.length < 8) {
            result = false
            errors.errorPassword = R.string.sign_in_input_password_error
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
}