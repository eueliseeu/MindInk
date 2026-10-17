package com.mindInk.app.ui.articles

import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi

data class ArticlesUiState(
    val isLoading: Boolean = true,
    val articles: List<ArticleCardUi> = emptyList(),
    val authors: List<AuthorUi> = emptyList(),
    val errorMessage: String? = null
)
