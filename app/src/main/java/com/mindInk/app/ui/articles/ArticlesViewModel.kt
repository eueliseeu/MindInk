package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.articles.mapper.toCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LATEST_LIMIT = 6
private const val REALTIME_LIMIT = 40L
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
    private var hasLoadedFirstPage = false

    init {
        observeRealtime()
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            observeArticles(REALTIME_LIMIT).collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val all = result.articles.map { it.toCardUi() }
                        val latest = all.take(LATEST_LIMIT)
                        val authors = result.articles
                            .distinctBy { it.authorUid }
                            .map { article ->
                                AuthorUi(
                                    uid = article.authorUid,
                                    name = formatAuthorName(article.authorName),
                                    photoUrl = article.authorPhotoUrl
                                )
                            }

                        _uiState.update { state ->
                            val explore = if (!hasLoadedFirstPage) {
                                hasLoadedFirstPage = true
                                exploreCursorId = all.lastOrNull()?.id
                                all
                            } else {
                                mergeRealtime(state.exploreArticles, all)
                            }

                            state.copy(
                                isLoading = false,
                                latestArticles = latest,
                                authors = authors,
                                exploreArticles = explore,
                                hasMoreExplore = all.size >= REALTIME_LIMIT.toInt() ||
                                    state.hasMoreExplore,
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
        if (!_uiState.value.hasMoreExplore) return
        if (exploreCursorId == null && _uiState.value.exploreArticles.isNotEmpty()) {
            exploreCursorId = _uiState.value.exploreArticles.lastOrNull()?.id
        }

        isLoadingMoreInternal = true
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            try {
                when (
                    val result = loadArticlesPage(
                        pageSize = EXPLORE_PAGE_SIZE,
                        startAfterDocumentId = exploreCursorId
                    )
                ) {
                    is LoadArticlesPageResult.Success -> {
                        val page = result.page
                        val newCards = page.articles.map { it.toCardUi() }
                        if (page.nextCursorId != null) {
                            exploreCursorId = page.nextCursorId
                        }
                        _uiState.update { state ->
                            state.copy(
                                isLoadingMore = false,
                                exploreArticles = appendUnique(state.exploreArticles, newCards),
                                hasMoreExplore = page.hasMore,
                                errorMessage = null
                            )
                        }
                    }
                    is LoadArticlesPageResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } finally {
                isLoadingMoreInternal = false
            }
        }
    }

    private fun mergeRealtime(
        current: List<ArticleCardUi>,
        observed: List<ArticleCardUi>
    ): List<ArticleCardUi> {
        if (observed.isEmpty()) return current
        val observedMap = observed.associateBy { it.id }
        val currentIds = current.map { it.id }.toHashSet()
        val brandNew = observed.filter { it.id !in currentIds }
        val updated = current.map { card -> observedMap[card.id] ?: card }
        return brandNew + updated
    }

    private fun appendUnique(
        current: List<ArticleCardUi>,
        incoming: List<ArticleCardUi>
    ): List<ArticleCardUi> {
        val ids = current.map { it.id }.toHashSet()
        return current + incoming.filter { it.id !in ids }
    }
}
