package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.data.article.toCardUi
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.ui.articles.model.AuthorUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LATEST_LIMIT = 6L
private const val EXPLORE_PAGE_SIZE = 10L

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val loadArticlesPage: LoadArticlesPageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private var exploreCursorId: String? = null
    private var isLoadingMoreInternal = false

    init {
        observeLatest()
        loadMoreExplore()
    }

    private fun observeLatest() {
        viewModelScope.launch {
            observeArticles.observeAll().collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val authors = result.articles
                            .distinctBy { it.authorUid }
                            .map { article ->
                                AuthorUi(
                                    uid = article.authorUid,
                                    name = formatAuthorName(article.authorName),
                                    photoUrl = article.authorPhotoUrl
                                )
                            }
                        val latest = result.articles
                            .take(LATEST_LIMIT.toInt())
                            .map { it.toCardUi() }
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                latestArticles = latest,
                                authors = authors,
                                exploreArticles = mergeStarCounts(state.exploreArticles, latest),
                                errorMessage = null
                            )
                        }
                    }
                    is ObserveArticlesResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }

    fun loadMoreExplore() {
        if (isLoadingMoreInternal) return
        if (!_uiState.value.hasMoreExplore && exploreCursorId != null) return

        isLoadingMoreInternal = true
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            when (
                val result = loadArticlesPage(
                    pageSize = EXPLORE_PAGE_SIZE,
                    startAfterDocumentId = exploreCursorId
                )
            ) {
                is LoadArticlesPageResult.Success -> {
                    val page = result.page
                    val newCards = page.articles.map { it.toCardUi() }
                    exploreCursorId = page.nextCursorId
                    _uiState.update { state ->
                        val merged = appendUnique(state.exploreArticles, newCards)
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            exploreArticles = merged,
                            hasMoreExplore = page.hasMore,
                            errorMessage = null
                        )
                    }
                }
                is LoadArticlesPageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
            isLoadingMoreInternal = false
        }
    }

    private fun appendUnique(
        current: List<com.mindInk.app.ui.articles.model.ArticleCardUi>,
        incoming: List<com.mindInk.app.ui.articles.model.ArticleCardUi>
    ): List<com.mindInk.app.ui.articles.model.ArticleCardUi> {
        val ids = current.map { it.id }.toHashSet()
        return current + incoming.filter { it.id !in ids }
    }

    private fun mergeStarCounts(
        explore: List<com.mindInk.app.ui.articles.model.ArticleCardUi>,
        latest: List<com.mindInk.app.ui.articles.model.ArticleCardUi>
    ): List<com.mindInk.app.ui.articles.model.ArticleCardUi> {
        if (explore.isEmpty() || latest.isEmpty()) return explore
        val stars = latest.associate { it.id to it.starCount }
        return explore.map { card ->
            stars[card.id]?.let { count -> card.copy(starCount = count) } ?: card
        }
    }
}
