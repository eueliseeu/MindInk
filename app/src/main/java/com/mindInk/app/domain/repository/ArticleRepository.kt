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

sealed interface UpdateAuthorArticlesResult {
    data object Success : UpdateAuthorArticlesResult
    data class Error(val message: String) : UpdateAuthorArticlesResult
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

sealed interface RecordArticleViewResult {
    data class Success(val viewCount: Int) : RecordArticleViewResult
    data class Error(val message: String) : RecordArticleViewResult
}

data class ArticlesPage(
    val articles: List<Article>,
    val nextCursorId: String?,
    val hasMore: Boolean
)

sealed interface LoadArticlesPageResult {
    data class Success(val page: ArticlesPage) : LoadArticlesPageResult
    data class Error(val message: String) : LoadArticlesPageResult
}

interface ArticleRepository {
    fun observeArticles(limit: Long = 50): Flow<ObserveArticlesResult>

    fun observeAllArticles(): Flow<ObserveArticlesResult>

    fun observeArticlesByAuthor(
        authorUid: String,
        limit: Long = 50
    ): Flow<ObserveArticlesResult>

    suspend fun loadArticlesPage(
        pageSize: Long = 10,
        startAfterDocumentId: String? = null
    ): LoadArticlesPageResult

    suspend fun getArticleById(articleId: String): Article?

    suspend fun createArticle(
        authorUid: String,
        authorName: String,
        authorUsername: String,
        authorPhotoUrl: String?,
        category: String,
        title: String,
        subtitle: String,
        content: String,
        linkUrl: String
    ): CreateArticleResult

    suspend fun updateArticle(
        articleId: String,
        authorUid: String,
        category: String,
        title: String,
        subtitle: String,
        content: String,
        linkUrl: String
    ): UpdateArticleResult

    suspend fun updateAuthorName(
        authorUid: String,
        authorName: String
    ): UpdateAuthorArticlesResult

    suspend fun deleteArticle(
        articleId: String,
        requesterUid: String
    ): DeleteArticleResult

    suspend fun toggleStar(
        articleId: String,
        uid: String
    ): ToggleStarResult

    suspend fun recordView(
        articleId: String,
        viewerUid: String
    ): RecordArticleViewResult
}
