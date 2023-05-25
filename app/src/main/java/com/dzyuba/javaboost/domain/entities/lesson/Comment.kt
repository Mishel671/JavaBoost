package com.dzyuba.javaboost.domain.entities.lesson

data class Comment(
    val id: Int,
    val userId: String,
    val userName: String,
    val userLogo: String? = null,
    val text: String
)