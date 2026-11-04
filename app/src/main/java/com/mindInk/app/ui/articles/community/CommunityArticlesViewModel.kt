package com.mindInk.app.ui.articles.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.ui.articles.mapper.mergeRealtimeArticles
import com.mindInk.app.ui.articles.mapper.toCardUi
import com.mindInk.app.ui.articles.model.ArticleCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10L
private const val REALTIME_LIMIT = 40L

@HiltViewModel
class CommunityArticlesViewModel @Inject constructor(
    private val loadArticlesPage: LoadArticlesPageUseCase,
    private val observeArticles: ObserveArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityArticlesUiState())
    val uiState: StateFlow<CommunityArticlesUiState> = _uiState.asStateFlow()

    private var cursorId: String? = null
    private var loadJob: Job? = null
    private var isLoadingMoreInternal = false

    init {
        refresh(clearList = true)
        observeRealtimeArticles()
    }

    fun onCategorySelected(category: String?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                hasMore = true,
                errorMessage = null
            )
        }
        cursorId = null
        refresh(clearList = false)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun loadMore() {
        if (isLoadingMoreInternal) return
        if (!_uiState.value.hasMore) return
        if (_uiState.value.isLoading) return

        isLoadingMoreInternal = true
        _uiState.update { it.copy(isLoadingMore = true) }

        loadJob = viewModelScope.launch {
            try {
                when (
                    val result = loadArticlesPage(
                        pageSize = PAGE_SIZE,
                        startAfterDocumentId = cursorId,
                        category = _uiState.value.selectedCategory
                    )
                ) {
                    is LoadArticlesPageResult.Success -> {
                        val page = result.page
                        val newCards = page.articles.map { it.toCardUi() }
                        if (page.nextCursorId != null) {
                            cursorId = page.nextCursorId
                        }
                        _uiState.update { state ->
                            state.copy(
                                isLoadingMore = false,
                                articles = appendUnique(state.articles, newCards),
                                hasMore = page.hasMore,
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

    fun refresh(clearList: Boolean = true) {
        loadJob?.cancel()
        isLoadingMoreInternal = false
        cursorId = null

        _uiState.update {
            it.copy(
                isLoading = true,
                isLoadingMore = false,
                articles = if (clearList) emptyList() else it.articles,
                hasMore = true,
                errorMessage = null
            )
        }

        val category = _uiState.value.selectedCategory
        loadJob = viewModelScope.launch {
            when (
                val result = loadArticlesPage(
                    pageSize = PAGE_SIZE,
                    startAfterDocumentId = null,
                    category = category
                )
            ) {
                is LoadArticlesPageResult.Success -> {
                    val page = result.page
                    cursorId = page.nextCursorId
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articles = page.articles.map { article -> article.toCardUi() },
                            hasMore = page.hasMore,
                            errorMessage = null
                        )
                    }
                }
                is LoadArticlesPageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articles = if (clearList) emptyList() else it.articles,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun observeRealtimeArticles() {
        viewModelScope.launch {
            observeArticles(REALTIME_LIMIT).collect { result ->
                if (result !is ObserveArticlesResult.Success) return@collect
                if (_uiState.value.isLoading) return@collect

                val observed = result.articles.map { it.toCardUi() }
                val windowFull = observed.size >= REALTIME_LIMIT.toInt()

                _uiState.update { state ->
                    state.copy(
                        articles = mergeRealtimeArticles(
                            current = state.articles,
                            observed = observed,
                            windowFull = windowFull,
                            category = state.selectedCategory
                        )
                    )
                }
                cursorId = _uiState.value.articles.lastOrNull()?.id
            }
        }
    }

    private fun appendUnique(
        current: List<ArticleCardUi>,
        incoming: List<ArticleCardUi>
    ): List<ArticleCardUi> {
        val ids = current.map { it.id }.toHashSet()
        return current + incoming.filter { it.id !in ids }
    }
}