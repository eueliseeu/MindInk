package com.mindInk.app.ui.articles.community

import com.mindInk.app.ui.articles.model.ArticleCardUi

data class CommunityArticlesUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val articles: List<ArticleCardUi> = emptyList(),
    val hasMore: Boolean = true,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val visibleArticles: List<ArticleCardUi>
        get() {
            val q = searchQuery.trim()
            if (q.isEmpty()) return articles
            return articles.filter { article ->
                article.title.contains(q, ignoreCase = true) ||
                    article.summary.contains(q, ignoreCase = true) ||
                    article.authorName.contains(q, ignoreCase = true)
            }
        }
}