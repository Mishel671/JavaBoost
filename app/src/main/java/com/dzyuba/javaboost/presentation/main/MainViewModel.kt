package com.dzyuba.javaboost.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    init {
        updateConnection()
    }
    fun updateConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateConnection()
        }
    }
}