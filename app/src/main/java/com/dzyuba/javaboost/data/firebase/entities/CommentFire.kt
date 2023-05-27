package com.dzyuba.javaboost.data.firebase.entities

data class CommentFire(
    val id: Long = -1,
    val userId: String? = null,
    val userName: String? = null,
    val userLogo: String? = null,
    val text: String? = null
)