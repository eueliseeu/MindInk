package com.mindInk.app.ui.articles

import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi

data class ArticlesUiState(
    val isLoading: Boolean = true,
    val latestArticles: List<ArticleCardUi> = emptyList(),
    val exploreArticles: List<ArticleCardUi> = emptyList(),
    val authors: List<AuthorUi> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMoreExplore: Boolean = true,
    val errorMessage: String? = null
)
