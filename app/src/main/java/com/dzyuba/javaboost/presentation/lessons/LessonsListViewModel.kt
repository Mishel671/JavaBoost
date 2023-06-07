package com.dzyuba.javaboost.presentation.lessons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class LessonsListViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableLiveData<Resource<List<LessonShort>>>()
    val lessons: LiveData<Resource<List<LessonShort>>>
        get() = _lessons

    fun loadAllLessons() {
        if (_lessons.value?.status != Resource.Status.SUCCESS) {
            _lessons.value = Resource.loading()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val deferredLessons = async {
                repository.getLessonsShort()
            }
            val deferredDecides = async {
                repository.getLessonsDecide()
            }
            val lessons = deferredLessons.await()
            val decides = deferredDecides.await()
            if (lessons.status == Resource.Status.SUCCESS && decides.status == Resource.Status.SUCCESS) {
                val lessonsList = lessons.data!!.toMutableList()
                val decidesList = decides.data!!
                decidesList.forEach { decide ->
                    val lessonItem = lessonsList.firstOrNull { it.id == decide.id }
                    lessonItem?.let { lesson ->
                        val totalProgress = decide.taskCount + 1
                        var progress = if (decide.wasRead) 1 else 0
                        decide.decides.forEach {
                            if (it.wasDecided) {
                                progress += 1
                            }
                        }
                        val percentProgress = progress * 100 / totalProgress
                        val changeItem = lesson.copy(progress = percentProgress)
                        lessonsList[changeItem.id.toInt()] = changeItem
                    }
                }
                _lessons.postValue(Resource.success(lessonsList))
            } else {
                _lessons.postValue(lessons)
            }
        }
    }

//    fun loadLessons(ids: List<Int>? = null) {
//        if (_lessons.value?.status != Resource.Status.SUCCESS) {
//            _lessons.value = Resource.loading()
//        }
//        viewModelScope.launch(Dispatchers.IO) {
//            if (ids == null) {
//                _lessons.postValue(repository.getLessonsShort())
//            } else {
//                _lessons.postValue(repository.getLessonsShortById(ids))
//            }
//        }
//    }
}