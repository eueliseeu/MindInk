package com.mindInk.app.ui.articles.model

data class AuthorUi(
    val uid: String,
    val name: String,
    val photoUrl: String?,
    val totalStars: Int = 0
)
