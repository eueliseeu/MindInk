package com.mindInk.app.ui.profile

import com.mindInk.app.ui.articles.model.ArticleCardUi

data class AuthorProfileUiState(
    val isLoading: Boolean = true,
    val authorUid: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPhotoUrl: String? = null,
    val githubUrl: String = "",
    val instagramUrl: String = "",
    val totalStars: Int = 0,
    val articles: List<ArticleCardUi> = emptyList(),
    val errorMessage: String? = null
)
