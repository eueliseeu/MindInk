package com.mindInk.app.ui.articles

import com.mindInk.app.ui.articles.model.ArticleCardUi

data class ArticlesUiState(
    val isLoading: Boolean = true,
    val articles: List<ArticleCardUi> = emptyList(),
    val errorMessage: String? = null
)
