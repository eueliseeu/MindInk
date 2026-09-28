package com.mindInk.app.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val username: String,
    val createdAt: Long = 0L
)
