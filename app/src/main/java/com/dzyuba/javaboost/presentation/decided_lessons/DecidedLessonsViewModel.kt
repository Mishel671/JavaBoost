package com.dzyuba.javaboost.presentation.decided_lessons

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.LessonDecides
import com.dzyuba.javaboost.domain.entities.LessonShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class DecidedLessonsViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableLiveData<Resource<List<LessonShort>>>()
    val lessons: LiveData<Resource<List<LessonShort>>>
        get() = _lessons

    private val _lessonsProgress = MutableLiveData<Int>()
    val lessonsProgress: LiveData<Int>
        get() = _lessonsProgress

    private var lessonsJob: Job? = null

    fun loadDecidedLessons() {
        if (_lessons.value?.status != Resource.Status.SUCCESS) {
            _lessons.value = Resource.loading()
        }
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch(Dispatchers.IO) {
            val deferredTaskCount = async {
                repository.getAllTaskCount()
            }
            val decides = repository.getLessonsDecide()
            val taskCount = deferredTaskCount.await()
            if (decides.data != null && taskCount.data != null) {
                val listId = decides.data.map { it.id }
                val deferredLessons = async {
                    repository.getLessonsShortById(listId)
                }
                val percent = calculateProgress(decides.data, taskCount.data)
                val lessons = deferredLessons.await()
                if (lessons.status == Resource.Status.SUCCESS) {
                    val lessonsList = lessons.data!!.toMutableList()
                    val decidesList = decides.data
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
                    _lessonsProgress.postValue(percent)
                } else {
                    _lessons.postValue(lessons)
                }
            } else {
                _lessons.postValue(Resource.error(Throwable("Lessons not found")))
            }
        }
    }

    private fun calculateProgress(
        lessonsDecides: List<LessonDecides>,
        lessonsTaskCount: List<Int>
    ): Int {
        var totalProgress = 0
        var progress = 0
        lessonsTaskCount.forEach {
            totalProgress += it + 1
        }
        lessonsDecides.forEach { lesson ->
            progress += 1
            lesson.decides.forEach {
                if (it.wasDecided) {
                    progress += 1
                }
            }
        }
        return if (progress > 0 || totalProgress > 0)
            progress * 100 / totalProgress
        else
            0
    }

}