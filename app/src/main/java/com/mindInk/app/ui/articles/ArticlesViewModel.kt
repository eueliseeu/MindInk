package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.ui.articles.mapper.toCardUi
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
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
private const val FAMOUS_AUTHORS_LIMIT = 12
private const val TOP_RATED_LIMIT = 10

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
                        val domain = result.articles
                        val all = domain.map { it.toCardUi() }
                        val latest = all.take(LATEST_LIMIT)

                        val authors = domain
                            .distinctBy { it.authorUid }
                            .map { article ->
                                AuthorUi(
                                    uid = article.authorUid,
                                    name = formatAuthorName(article.authorName),
                                    photoUrl = article.authorPhotoUrl
                                )
                            }

                        val famousAuthors = domain
                            .groupBy { it.authorUid }
                            .map { (_, list) ->
                                val first = list.first()
                                AuthorUi(
                                    uid = first.authorUid,
                                    name = formatAuthorName(first.authorName),
                                    photoUrl = first.authorPhotoUrl,
                                    totalStars = list.sumOf { it.starCount }
                                )
                            }
                            .sortedByDescending { it.totalStars }
                            .take(FAMOUS_AUTHORS_LIMIT)

                        val topRated = all
                            .sortedByDescending { it.starCount }
                            .take(TOP_RATED_LIMIT)

                        val windowFull = all.size >= REALTIME_LIMIT.toInt()

                        _uiState.update { state ->
                            val explore = if (!hasLoadedFirstPage) {
                                hasLoadedFirstPage = true
                                all
                            } else {
                                mergeRealtime(state.exploreArticles, all, windowFull)
                            }
                            exploreCursorId = explore.lastOrNull()?.id

                            state.copy(
                                isLoading = false,
                                latestArticles = latest,
                                authors = authors,
                                famousAuthors = famousAuthors,
                                topRatedArticles = topRated,
                                exploreArticles = explore,
                                hasMoreExplore = windowFull || state.hasMoreExplore,
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
        observed: List<ArticleCardUi>,
        windowFull: Boolean
    ): List<ArticleCardUi> {
        val observedMap = observed.associateBy { it.id }
        val oldestObserved = observed.minOfOrNull { it.createdAt }
        val currentIds = current.map { it.id }.toHashSet()

        val kept = current.filter { card ->
            when {
                card.id in observedMap -> true
                !windowFull -> false
                oldestObserved == null -> true
                else -> card.createdAt < oldestObserved
            }
        }
        val updated = kept.map { card -> observedMap[card.id] ?: card }
        val brandNew = observed.filter { it.id !in currentIds }

        return (brandNew + updated).sortedByDescending { it.createdAt }
    }

    private fun appendUnique(
        current: List<ArticleCardUi>,
        incoming: List<ArticleCardUi>
    ): List<ArticleCardUi> {
        val ids = current.map { it.id }.toHashSet()
        return current + incoming.filter { it.id !in ids }
    }
}
