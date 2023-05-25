package com.dzyuba.javaboost.presentation.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.ProfileRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>>
        get() = _profile

    private var profileJob: Job? = null

    private var updateImageJob: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (profileJob?.isActive != true) {
            profileJob = viewModelScope.launch(Dispatchers.IO) {
                if (_profile.value?.status != Resource.Status.SUCCESS) {
                    _profile.postValue(Resource.loading())
                }
                _profile.postValue(repository.loadUser())
            }
        }
    }


    fun updateImage(bitmap: Bitmap) {
        updateImageJob?.cancel()
        profileJob?.cancel()
        updateImageJob = viewModelScope.launch(Dispatchers.IO) {
            _profile.postValue(Resource.loading())
            val resultImage = repository.updateProfileImage(bitmap)
            if (resultImage.status == Resource.Status.SUCCESS) {
                _profile.postValue(repository.loadUser())
            } else {
                _profile.postValue(Resource.error(resultImage.error!!))
            }
        }
    }

    fun logout() {
        repository.logout()
    }
}