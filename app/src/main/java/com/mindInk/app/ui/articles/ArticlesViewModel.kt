package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.data.reading.PendingReadingStore
import com.mindInk.app.domain.model.Article
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.repository.ObserveReadingResult
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.domain.usecase.reading.ObserveContinueReadingUseCase
import com.mindInk.app.domain.usecase.reading.UpsertReadingProgressUseCase
import com.mindInk.app.ui.articles.mapper.toCardUi
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.articles.model.MuralItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
private const val MURAL_LIMIT = 3
private const val CONTINUE_READING_LIMIT = 5L

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val loadArticlesPage: LoadArticlesPageUseCase,
    private val observeContinueReading: ObserveContinueReadingUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val getArticleById: GetArticleByIdUseCase,
    private val pendingReadingStore: PendingReadingStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private var exploreCursorId: String? = null
    private var isLoadingMoreInternal = false
    private var hasLoadedFirstPage = false
    private var currentUid: String? = null
    private var lastDomainArticles: List<Article> = emptyList()
    private var continueReadingJob: Job? = null

    init {
        observeRealtime()
    }

    fun bindUser(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        _uiState.update { state ->
            state.copy(muralItems = buildMuralItems(lastDomainArticles, uid))
        }
        startContinueReading(uid)
        flushPendingReading(uid)
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            observeArticles(REALTIME_LIMIT).collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val domain = result.articles
                        lastDomainArticles = domain
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
                        val mural = currentUid?.let { buildMuralItems(domain, it) }.orEmpty()

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
                                muralItems = mural,
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

    private fun startContinueReading(uid: String) {
        continueReadingJob?.cancel()
        continueReadingJob = viewModelScope.launch {
            observeContinueReading(uid, CONTINUE_READING_LIMIT).collect { result ->
                when (result) {
                    is ObserveReadingResult.Success -> {
                        val cards = result.items.map { item ->
                            ArticleCardUi(
                                id = item.articleId,
                                category = item.category.ifBlank { "Tech" },
                                title = item.title,
                                summary = "",
                                authorName = formatAuthorName(item.authorName),
                                authorPhotoUrl = item.authorPhotoUrl,
                                starCount = 0,
                                viewCount = 0,
                                starredBy = emptyList(),
                                createdAt = item.updatedAt
                            )
                        }
                        _uiState.update { it.copy(continueReading = cards) }
                    }
                    is ObserveReadingResult.Error -> Unit
                }
            }
        }
    }

    private fun flushPendingReading(uid: String) {
        viewModelScope.launch {
            val pending = pendingReadingStore.getOnce()
            if (pending.isEmpty()) return@launch
            pending.forEach { articleId ->
                val article = getArticleById(articleId) ?: return@forEach
                upsertReadingProgress(
                    uid = uid,
                    articleId = article.id,
                    title = article.title,
                    category = article.category,
                    authorName = article.authorName,
                    authorPhotoUrl = article.authorPhotoUrl,
                    progress = 0f
                )
            }
            pendingReadingStore.clear()
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

    private fun buildMuralItems(
        articles: List<Article>,
        uid: String
    ): List<MuralItemUi> {
        if (articles.isEmpty()) return emptyList()

        val authorLookup = articles
            .groupBy { it.authorUid }
            .mapValues { (_, list) -> list.first() }

        val events = mutableListOf<MuralItemUi>()

        articles
            .filter { it.authorUid == uid }
            .forEach { article ->
                article.starredBy
                    .filter { starerUid -> starerUid != uid }
                    .forEach { starerUid ->
                        val starer = authorLookup[starerUid]
                        events += MuralItemUi.StarReceived(
                            id = "star_${article.id}_$starerUid",
                            actorUid = starerUid,
                            actorName = formatAuthorName(starer?.authorName),
                            actorPhotoUrl = starer?.authorPhotoUrl,
                            articleId = article.id,
                            articleTitle = article.title,
                            sortKey = maxOf(article.updatedAt, article.createdAt)
                        )
                    }
            }

        val favoriteAuthorUids = articles
            .filter { uid in it.starredBy }
            .map { it.authorUid }
            .filter { it != uid }
            .toSet()

        articles
            .filter { it.authorUid in favoriteAuthorUids }
            .forEach { article ->
                events += MuralItemUi.NewArticleFromFavorite(
                    id = "new_${article.id}",
                    actorUid = article.authorUid,
                    actorName = formatAuthorName(article.authorName),
                    actorPhotoUrl = article.authorPhotoUrl,
                    articleId = article.id,
                    articleTitle = article.title,
                    sortKey = article.createdAt
                )
            }

        return events
            .sortedByDescending { it.sortKey }
            .distinctBy { it.id }
            .take(MURAL_LIMIT)
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
