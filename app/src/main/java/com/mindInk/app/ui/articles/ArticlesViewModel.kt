package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.data.reading.PendingReadingStore
import com.mindInk.app.domain.model.Article
import com.mindInk.app.domain.model.FavoriteAuthor
import com.mindInk.app.domain.model.ReceivedStar
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.repository.ObserveFavoriteAuthorsResult
import com.mindInk.app.domain.repository.ObserveReadingResult
import com.mindInk.app.domain.repository.ObserveReceivedStarsResult
import com.mindInk.app.domain.repository.PublicUserProfile
import com.mindInk.app.domain.repository.UserProfileRepository
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.domain.usecase.favorite.ObserveFavoriteAuthorsUseCase
import com.mindInk.app.domain.usecase.reading.ObserveContinueReadingUseCase
import com.mindInk.app.domain.usecase.reading.UpsertReadingProgressUseCase
import com.mindInk.app.domain.usecase.star.ObserveReceivedStarsUseCase
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
private const val TOP_RATED_LIMIT = 10
private const val MURAL_LIMIT = 3
private const val RECEIVED_STARS_LIMIT = 10L
private const val CONTINUE_READING_LIMIT = 5L
private const val FALLBACK_ACTOR_NAME = "UserMind"

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val loadArticlesPage: LoadArticlesPageUseCase,
    private val observeContinueReading: ObserveContinueReadingUseCase,
    private val observeFavoriteAuthors: ObserveFavoriteAuthorsUseCase,
    private val observeReceivedStars: ObserveReceivedStarsUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val getArticleById: GetArticleByIdUseCase,
    private val pendingReadingStore: PendingReadingStore,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private var exploreCursorId: String? = null
    private var isLoadingMoreInternal = false
    private var hasLoadedFirstPage = false
    private var currentUid: String? = null
    private var lastDomainArticles: List<Article> = emptyList()
    private var favoritedAtByAuthor: Map<String, Long> = emptyMap()
    private var receivedStars: List<ReceivedStar> = emptyList()
    private var continueReadingJob: Job? = null
    private var favoriteAuthorsJob: Job? = null
    private var receivedStarsJob: Job? = null
    private var muralJob: Job? = null

    private val profileCache = mutableMapOf<String, PublicUserProfile>()
    private val titleCache = mutableMapOf<String, String>()

    init {
        observeRealtime()
    }

    fun bindUser(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        refreshMural(lastDomainArticles, uid)
        startFavoriteAuthors(uid)
        startReceivedStars(uid)
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
                                topRatedArticles = topRated,
                                exploreArticles = explore,
                                hasMoreExplore = windowFull || state.hasMoreExplore,
                                errorMessage = null
                            )
                        }

                        currentUid?.let { uid ->
                            refreshMural(domain, uid)
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

    private fun startFavoriteAuthors(uid: String) {
        favoriteAuthorsJob?.cancel()
        favoriteAuthorsJob = viewModelScope.launch {
            observeFavoriteAuthors(uid).collect { result ->
                when (result) {
                    is ObserveFavoriteAuthorsResult.Success -> {
                        favoritedAtByAuthor = result.authors.associate { it.uid to it.favoritedAt }
                        _uiState.update { state ->
                            state.copy(favoriteAuthors = result.authors.map { it.toAuthorUi() })
                        }
                        refreshMural(lastDomainArticles, uid)
                    }
                    is ObserveFavoriteAuthorsResult.Error -> Unit
                }
            }
        }
    }

    private fun startReceivedStars(uid: String) {
        receivedStarsJob?.cancel()
        receivedStars = emptyList()
        receivedStarsJob = viewModelScope.launch {
            observeReceivedStars(uid, RECEIVED_STARS_LIMIT).collect { result ->
                when (result) {
                    is ObserveReceivedStarsResult.Success -> {
                        receivedStars = result.stars
                        refreshMural(lastDomainArticles, uid)
                    }
                    is ObserveReceivedStarsResult.Error -> Unit
                }
            }
        }
    }

    private fun refreshMural(articles: List<Article>, uid: String) {
        val stars = receivedStars
        val favoritedAt = favoritedAtByAuthor
        muralJob?.cancel()
        muralJob = viewModelScope.launch {
            val items = buildMuralItems(articles, uid, stars, favoritedAt)
            _uiState.update { it.copy(muralItems = items) }
        }
    }

    private fun startContinueReading(uid: String) {
        continueReadingJob?.cancel()
        continueReadingJob = viewModelScope.launch {
            observeContinueReading(uid, CONTINUE_READING_LIMIT).collect { result ->
                when (result) {
                    is ObserveReadingResult.Success -> {
                        val cards = result.items.map { item ->
                            val live = lastDomainArticles.firstOrNull { it.id == item.articleId }
                            ArticleCardUi(
                                id = item.articleId,
                                category = (live?.category ?: item.category).ifBlank { "Tech" },
                                title = live?.title ?: item.title,
                                summary = live?.subtitle?.takeIf { it.isNotBlank() }
                                    ?: live?.preview(120)
                                    ?: item.summary,
                                authorName = formatAuthorName(live?.authorName ?: item.authorName),
                                authorPhotoUrl = live?.authorPhotoUrl ?: item.authorPhotoUrl,
                                starCount = live?.starCount ?: item.starCount,
                                viewCount = live?.viewCount ?: item.viewCount,
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
                    summary = article.subtitle.ifBlank { article.preview(120) },
                    authorName = article.authorName,
                    authorPhotoUrl = article.authorPhotoUrl,
                    starCount = article.starCount,
                    viewCount = article.viewCount,
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

    private suspend fun buildMuralItems(
        articles: List<Article>,
        uid: String,
        receivedStars: List<ReceivedStar>,
        favoritedAtByAuthor: Map<String, Long>
    ): List<MuralItemUi> {
        val articlesById = articles.associateBy { it.id }

        val stars = receivedStars
            .filter { it.starerUid != uid }
            .take(MURAL_LIMIT)

        val missingProfiles = stars
            .map { it.starerUid }
            .distinct()
            .filter { it !in profileCache }
        if (missingProfiles.isNotEmpty()) {
            val fetched = userProfileRepository.getProfiles(missingProfiles)
            missingProfiles.forEach { starerUid ->
                profileCache[starerUid] = fetched[starerUid]
                    ?: PublicUserProfile(uid = starerUid, name = "")
            }
        }

        val starEvents = stars.mapNotNull { star ->
            val title = articlesById[star.articleId]?.title
                ?: titleCache[star.articleId]
                ?: getArticleById(star.articleId)?.title?.also { titleCache[star.articleId] = it }
                ?: return@mapNotNull null
            val profile = profileCache[star.starerUid]

            MuralItemUi.StarReceived(
                id = "star_${star.articleId}_${star.starerUid}",
                actorUid = star.starerUid,
                actorName = profile?.name
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::formatAuthorName)
                    ?: FALLBACK_ACTOR_NAME,
                actorPhotoUrl = profile?.photoUrl,
                articleId = star.articleId,
                articleTitle = title,
                sortKey = star.createdAt
            )
        }

        val newArticleEvents = articles.mapNotNull { article ->
            val favoritedAt = favoritedAtByAuthor[article.authorUid] ?: return@mapNotNull null
            if (article.createdAt <= favoritedAt) return@mapNotNull null
            MuralItemUi.NewArticleFromFavorite(
                id = "new_${article.id}",
                actorUid = article.authorUid,
                actorName = formatAuthorName(article.authorName),
                actorPhotoUrl = article.authorPhotoUrl,
                articleId = article.id,
                articleTitle = article.title,
                sortKey = article.createdAt
            )
        }

        return (starEvents + newArticleEvents)
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

    private fun FavoriteAuthor.toAuthorUi(): AuthorUi = AuthorUi(
        uid = uid,
        name = formatAuthorName(name),
        photoUrl = photoUrl
    )
}