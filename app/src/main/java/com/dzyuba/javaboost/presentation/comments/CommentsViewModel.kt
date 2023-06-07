package com.dzyuba.javaboost.presentation.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.Resource
import com.dzyuba.javaboost.domain.entities.lesson.Comment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentsViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessonList = MutableLiveData<Resource<List<Comment>>>(null)
    val lessonList: LiveData<Resource<List<Comment>>>
        get() = _lessonList

    private val _sendComment = MutableLiveData<Resource<Unit>>()
    val sendComment: LiveData<Resource<Unit>>
        get() = _sendComment

    private var commentJob: Job? = null

    val userId get() = repository.getUserId()

    fun subscribeToComments(lessonId: Long) {
        if (_lessonList.value == null) {
            _lessonList.value = Resource.loading()
            viewModelScope.launch(Dispatchers.IO) {
                repository.getLessonComments(lessonId).collect {
                    _lessonList.postValue(it)
                }
            }
        }
    }

    fun sendComment(lessondId: Long, inputComment: String?) {
        if (commentJob?.isActive != true) {
            val comment = inputComment?.trim() ?: ""
            if (comment.isNotEmpty()) {
                commentJob = viewModelScope.launch {
                    _sendComment.postValue(repository.sendComment(lessondId, comment))
                }
            }
        }
    }
}