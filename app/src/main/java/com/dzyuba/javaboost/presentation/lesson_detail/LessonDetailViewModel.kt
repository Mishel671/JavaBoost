package com.dzyuba.javaboost.presentation.lesson_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.dzyuba.javaboost.domain.entities.lesson.Test
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.util.Event
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LessonDetailViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lesson = MutableLiveData<Resource<Lesson>>()
    val lesson: LiveData<Resource<Lesson>>
        get() = _lesson

    private val _rate = MutableLiveData<Resource<Unit>>()
    val rate: LiveData<Resource<Unit>>
        get() = _rate

    val userId: String
        get() = repository.getUserId()

    fun loadLesson(lessonId: Int) {
        _lesson.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            _lesson.postValue(repository.getLessonsDetail(lessonId))
        }
    }

    fun setRate(lessonId: Int, rate: Float) {
        _rate.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            _rate.postValue(repository.setLessonRate(lessonId, rate))
        }
    }

    fun setAnswer(answerId: Int, itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _lesson.value?.data?.let { lesson ->
                val lessonsList = lesson.lessonItems
                if (lessonsList.size - 1 >= itemId) {
                    val item = lessonsList[itemId]
                    if (item is Test) {
                        val changeTest = item.copy(answerResult = answerId )
                        val newList = lessonsList.toMutableList().apply { set(itemId, changeTest) }
                        _lesson.postValue(Resource.success(lesson.copy(lessonItems = newList)))
                    }
                }
            }
        }
    }
}