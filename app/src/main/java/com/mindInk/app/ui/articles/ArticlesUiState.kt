package com.mindInk.app.ui.articles

import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.articles.model.MuralItemUi

data class ArticlesUiState(
    val isLoading: Boolean = true,
    val latestArticles: List<ArticleCardUi> = emptyList(),
    val exploreArticles: List<ArticleCardUi> = emptyList(),
    val authors: List<AuthorUi> = emptyList(),
    val famousAuthors: List<AuthorUi> = emptyList(),
    val topRatedArticles: List<ArticleCardUi> = emptyList(),
    val muralItems: List<MuralItemUi> = emptyList(),
    val continueReading: List<ArticleCardUi> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMoreExplore: Boolean = true,
    val errorMessage: String? = null
)