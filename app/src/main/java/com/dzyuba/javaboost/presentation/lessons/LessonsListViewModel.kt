package com.dzyuba.javaboost.presentation.lessons

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.ProfileRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LessonsListViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableLiveData<Resource<List<LessonShort>>>()
    val lessons: LiveData<Resource<List<LessonShort>>>
        get() = _lessons

    fun loadLessons(ids: List<Int>? = null) {
        if (_lessons.value?.status != Resource.Status.SUCCESS) {
            _lessons.value = Resource.loading()
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (ids == null) {
                _lessons.postValue(repository.getLessonsShort())
            } else {
                _lessons.postValue(repository.getLessonsShortById(ids))
            }
        }
    }
}