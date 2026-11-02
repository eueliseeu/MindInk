package com.mindInk.app.ui.articles.detail

import com.mindInk.app.domain.model.Article

data class ArticleDetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
    val currentUid: String? = null,
    val isStarredByMe: Boolean = false,
    val errorMessage: String? = null,
    val toastMessage: String? = null
)