package com.mindInk.app.data.favorite

data class FavoriteAuthorDto(
    val authorUid: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val starredCount: Int = 0,
    val favoritedAt: Long = 0L
)