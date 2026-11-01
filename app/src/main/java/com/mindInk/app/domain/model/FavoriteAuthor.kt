package com.mindInk.app.domain.model

data class FavoriteAuthor(
    val uid: String,
    val name: String,
    val photoUrl: String?,
    val favoritedAt: Long
)