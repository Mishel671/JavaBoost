package com.dzyuba.javaboost.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _needScreen = MutableLiveData<NeedScreen>()
    val needScreen: LiveData<NeedScreen>
        get() = _needScreen

    init {
        checkAuthentication()
    }

    fun checkAuthentication() {
        if (repository.isAuthenticated()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.loadUser().ifSuccess {
                    if (it != null) {
                        val user = repository.getUser()
                        if (!user.isEmailVerified) {
                            _needScreen.postValue(NeedScreen.EMAIL_VERIFIED)
                        } else if (user.name?.isEmpty() != false) {
                            _needScreen.postValue(NeedScreen.NICKNAME)
                        } else {
                            _needScreen.postValue(NeedScreen.MAIN_SCREEN)
                        }
                    } else {
                        _needScreen.postValue(NeedScreen.SIGN_IN)
                    }
                }.ifError {
                    _needScreen.postValue(NeedScreen.ERROR.apply { error = it.cause })
                }
            }
        } else {
            _needScreen.value = NeedScreen.SIGN_IN
        }
    }

}