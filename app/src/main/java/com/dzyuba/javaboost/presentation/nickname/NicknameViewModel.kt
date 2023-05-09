package com.dzyuba.javaboost.presentation.nickname

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.domain.FirebaseRepository
import com.dzyuba.javaboost.domain.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class NicknameViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _changeNickname = MutableLiveData<Resource<Unit>>()
    val changeNickname: LiveData<Resource<Unit>>
        get() = _changeNickname

    private val _inputError = MutableLiveData<Int?>()
    val inputError: LiveData<Int?>
        get() = _inputError

    fun setNickname(inputName: String?) {
        val name = inputName?.trim() ?: ""
        if (name.isNotEmpty()) {
            if (name.length <= 16) {
                _changeNickname.postValue(Resource.loading())
                viewModelScope.launch {
                    _changeNickname.postValue(repository.updateProfileName(name))
                }
            } else {
                _inputError.value = R.string.nickname_input_many_symbols
            }
        } else {
            _inputError.value = R.string.nickname_input_empty
        }
    }

    fun resetNickname(){
        _inputError.value = null
    }
}