package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.Article
import kotlinx.coroutines.flow.Flow

sealed interface CreateArticleResult {
    data class Success(val articleId: String) : CreateArticleResult
    data class Error(val message: String) : CreateArticleResult
}

sealed interface UpdateArticleResult {
    data object Success : UpdateArticleResult
    data class Error(val message: String) : UpdateArticleResult
}

sealed interface DeleteArticleResult {
    data object Success : DeleteArticleResult
    data class Error(val message: String) : DeleteArticleResult
}

sealed interface ObserveArticlesResult {
    data class Success(val articles: List<Article>) : ObserveArticlesResult
    data class Error(val message: String) : ObserveArticlesResult
}

sealed interface ToggleStarResult {
    data object Success : ToggleStarResult
    data class Error(val message: String) : ToggleStarResult
}

interface ArticleRepository {
    fun observeArticles(limit: Long = 50): Flow<ObserveArticlesResult>

    suspend fun getArticleById(articleId: String): Article?

    suspend fun createArticle(
        authorUid: String,
        authorName: String,
        authorUsername: String,
        title: String,
        content: String,
        linkUrl: String
    ): CreateArticleResult

    suspend fun updateArticle(
        articleId: String,
        authorUid: String,
        title: String,
        content: String,
        linkUrl: String
    ): UpdateArticleResult

    suspend fun deleteArticle(
        articleId: String,
        requesterUid: String
    ): DeleteArticleResult

    suspend fun toggleStar(
        articleId: String,
        uid: String
    ): ToggleStarResult
}
