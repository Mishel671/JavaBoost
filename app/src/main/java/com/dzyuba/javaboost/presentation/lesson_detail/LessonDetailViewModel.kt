package com.dzyuba.javaboost.presentation.lesson_detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.lesson.Lesson
import com.dzyuba.javaboost.domain.entities.lesson.LessonItem
import com.dzyuba.javaboost.domain.entities.lesson.Practice
import com.dzyuba.javaboost.domain.entities.lesson.Test
import com.dzyuba.javaboost.domain.entities.lesson.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    var toolbarIsExpanded = true
    val userId: String
        get() = repository.getUserId()

    private var taskCount = 0

    private var wasReaded = false

    fun loadLesson(lessonId: Long) {
        if (_lesson.value?.status != Resource.Status.SUCCESS) {
            _lesson.value = Resource.loading()
            viewModelScope.launch(Dispatchers.IO) {
                val deferredLessons = async {
                    repository.getLessonsDetail(lessonId)
                }
                val deferredDecideLessons = async {
                    repository.getDecidesList(lessonId)
                }
                val lessons = deferredLessons.await()
                val decideLessons = deferredDecideLessons.await()
                Log.d("MainLog", "Lesson Decide list: ${decideLessons.data!!}\n ")
                launch {
                    lessons.data?.lessonItems?.let { lessonItem ->
                        taskCount = 0
                        lessonItem.forEach {
                            if (it is Test || it is Practice)
                                taskCount += 1
                        }
                    }
                }
                if (lessons.status == Resource.Status.SUCCESS && decideLessons.status == Resource.Status.SUCCESS) {
                    val lessonList = lessons.data!!.lessonItems.toMutableList()
                    val decideList = decideLessons.data!!
                    decideList.forEach { decide ->
                        val lessonItem = lessonList.firstOrNull { it.id == decide.id }
                        when (lessonItem) {
                            is Test -> {
                                decide.trueAnswerId?.let { trueAnswerId ->
                                    val changeLessonItem =
                                        lessonItem.copy(answerResult = trueAnswerId)
                                    lessonList[changeLessonItem.id.toInt()] = changeLessonItem
                                }
                            }

                            is Practice -> {
                                val changeLessonItem =
                                    lessonItem.copy(wasDecided = decide.wasDecided)
                                lessonList[changeLessonItem.id.toInt()] = changeLessonItem
                            }

                            else -> {}
                        }
                    }
                    _lesson.postValue(Resource.success(lessons.data.copy(lessonItems = lessonList)))

                } else {
                    _lesson.postValue(lessons)
                }
            }
        }
    }


    fun setRate(lessonId: Long, rate: Float) {
        _rate.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            _rate.postValue(repository.setLessonRate(lessonId, rate))
        }
    }

    fun setAnswer(answerId: Int, itemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _lesson.value?.data?.let { lesson ->
                val lessonsList = lesson.lessonItems
                if (lessonsList.size - 1 >= itemId) {
                    val item = lessonsList[itemId.toInt()]
                    if (item is Test) {
                        val changeTest = item.copy(answerResult = answerId)
                        val newList =
                            lessonsList.toMutableList().apply { set(itemId.toInt(), changeTest) }
                        _lesson.postValue(Resource.success(lesson.copy(lessonItems = newList)))
                        repository.setDecideTask(lesson.id, changeTest, taskCount)
                    }
                }
            }
        }
    }


    fun setPracticeAnswer(answerPractice: Pair<Long, Boolean>?) {
        if (answerPractice != null) {
            val itemId = answerPractice.first
            Log.d(
                "MainLog",
                "Lessons: ${_lesson.value}, lesson list size: ${_lesson.value?.data?.lessonItems?.size}"
            )
            viewModelScope.launch(Dispatchers.IO) {
                _lesson.value?.data?.let { lesson ->
                    val lessonsList = lesson.lessonItems
                    if (lessonsList.size - 1 >= itemId) {
                        val item = lessonsList[itemId.toInt()]
                        if (item is Practice) {
                            val changePractice = item.copy(wasDecided = answerPractice.second)
                            val newList =
                                lessonsList.toMutableList()
                                    .apply { set(itemId.toInt(), changePractice) }
                            _lesson.postValue(Resource.success(lesson.copy(lessonItems = newList)))
                            Log.d("MainLog", "Practice: ${changePractice}")
                            repository.setDecideTask(lesson.id, changePractice, taskCount)
                        }
                    }
                }
            }
        }
    }

    fun setRead() {
        viewModelScope.launch(Dispatchers.IO) {
            _lesson.value?.data?.let {
                if (!wasReaded) {
                    repository.setDecideTask(it.id, null, taskCount)
                    wasReaded = true
                }
            }
        }
    }
}