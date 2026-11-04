package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.data.reading.PendingReadingStore
import com.mindInk.app.domain.model.Article
import com.mindInk.app.domain.model.FavoriteAuthor
import com.mindInk.app.domain.model.ReadingProgress
import com.mindInk.app.domain.model.ReceivedStar
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.repository.ObserveFavoriteAuthorsResult
import com.mindInk.app.domain.repository.ObserveReadingResult
import com.mindInk.app.domain.repository.ObserveReceivedStarsResult
import com.mindInk.app.domain.repository.PublicUserProfile
import com.mindInk.app.domain.repository.UserProfileRepository
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.GetArticlesByIdsUseCase
import com.mindInk.app.domain.usecase.article.LoadArticlesPageUseCase
import com.mindInk.app.domain.usecase.article.ObserveArticlesByAuthorsUseCase
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
private const val FAVORITE_ARTICLES_LIMIT = 20L
private const val CONTINUE_READING_LIMIT = 5L
private const val FALLBACK_NAME = "UserMind"

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val observeArticlesByAuthors: ObserveArticlesByAuthorsUseCase,
    private val loadArticlesPage: LoadArticlesPageUseCase,
    private val observeContinueReading: ObserveContinueReadingUseCase,
    private val observeFavoriteAuthors: ObserveFavoriteAuthorsUseCase,
    private val observeReceivedStars: ObserveReceivedStarsUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val getArticleById: GetArticleByIdUseCase,
    private val getArticlesByIds: GetArticlesByIdsUseCase,
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
    private var favoriteArticles: List<Article> = emptyList()
    private var receivedStars: List<ReceivedStar> = emptyList()
    private var readingProgress: List<ReadingProgress> = emptyList()

    private var favoriteAuthorsJob: Job? = null
    private var favoriteArticlesJob: Job? = null
    private var receivedStarsJob: Job? = null
    private var continueReadingJob: Job? = null
    private var muralJob: Job? = null
    private var readingCardsJob: Job? = null

    private val profileCache = mutableMapOf<String, PublicUserProfile>()
    private val titleCache = mutableMapOf<String, String>()
    private val readingArticleCache = mutableMapOf<String, Article>()

    init {
        observeRealtime()
    }

    fun bindUser(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        resetUserState()
        startFavoriteAuthors(uid)
        startReceivedStars(uid)
        startContinueReading(uid)
        flushPendingReading(uid)
    }

    private fun resetUserState() {
        favoriteAuthorsJob?.cancel()
        favoriteArticlesJob?.cancel()
        receivedStarsJob?.cancel()
        continueReadingJob?.cancel()
        muralJob?.cancel()
        readingCardsJob?.cancel()

        favoritedAtByAuthor = emptyMap()
        favoriteArticles = emptyList()
        receivedStars = emptyList()
        readingProgress = emptyList()
        readingArticleCache.clear()

        _uiState.update {
            it.copy(
                favoriteAuthors = emptyList(),
                muralItems = emptyList(),
                continueReading = emptyList()
            )
        }
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
                            refreshMural(uid)
                            refreshContinueReadingCards()
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
        favoriteAuthorsJob = viewModelScope.launch {
            observeFavoriteAuthors(uid).collect { result ->
                if (result !is ObserveFavoriteAuthorsResult.Success) return@collect

                val authors = result.authors
                favoritedAtByAuthor = authors.associate { it.uid to it.favoritedAt }

                val profiles = loadProfiles(authors.map { it.uid })
                _uiState.update { state ->
                    state.copy(
                        favoriteAuthors = authors.map { it.toAuthorUi(profiles[it.uid]) }
                    )
                }

                startFavoriteArticles(uid, authors.map { it.uid })
                refreshMural(uid)
            }
        }
    }

    private fun startFavoriteArticles(uid: String, authorUids: List<String>) {
        favoriteArticlesJob?.cancel()
        if (authorUids.isEmpty()) {
            favoriteArticles = emptyList()
            return
        }
        favoriteArticlesJob = viewModelScope.launch {
            observeArticlesByAuthors(authorUids, FAVORITE_ARTICLES_LIMIT).collect { result ->
                if (result is ObserveArticlesResult.Success) {
                    favoriteArticles = result.articles
                    refreshMural(uid)
                }
            }
        }
    }

    private fun startReceivedStars(uid: String) {
        receivedStarsJob = viewModelScope.launch {
            observeReceivedStars(uid, MURAL_LIMIT.toLong()).collect { result ->
                if (result is ObserveReceivedStarsResult.Success) {
                    receivedStars = result.stars
                    refreshMural(uid)
                }
            }
        }
    }

    private fun refreshMural(uid: String) {
        if (currentUid != uid) return
        val stars = receivedStars
        val newArticles = favoriteArticles
        val favoritedAt = favoritedAtByAuthor

        muralJob?.cancel()
        muralJob = viewModelScope.launch {
            val items = buildMuralItems(stars, newArticles, favoritedAt)
            _uiState.update { it.copy(muralItems = items) }
        }
    }

    private suspend fun buildMuralItems(
        stars: List<ReceivedStar>,
        favoriteArticles: List<Article>,
        favoritedAtByAuthor: Map<String, Long>
    ): List<MuralItemUi> {
        val profiles = loadProfiles(stars.map { it.starerUid })
        val articlesById = lastDomainArticles.associateBy { it.id }

        val starEvents = stars.mapNotNull { star ->
            val title = articlesById[star.articleId]?.title
                ?: titleCache[star.articleId]
                ?: getArticleById(star.articleId)?.title?.also { titleCache[star.articleId] = it }
                ?: return@mapNotNull null
            val profile = profiles[star.starerUid]

            MuralItemUi.StarReceived(
                id = "star_${star.articleId}_${star.starerUid}",
                actorUid = star.starerUid,
                actorName = profile.displayNameOrFallback(),
                actorPhotoUrl = profile?.photoUrl,
                articleId = star.articleId,
                articleTitle = title,
                sortKey = star.createdAt
            )
        }

        val newArticleEvents = favoriteArticles.mapNotNull { article ->
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

    private fun startContinueReading(uid: String) {
        continueReadingJob = viewModelScope.launch {
            observeContinueReading(uid, CONTINUE_READING_LIMIT).collect { result ->
                when (result) {
                    is ObserveReadingResult.Success -> {
                        readingProgress = result.items
                        refreshContinueReadingCards()
                    }
                    is ObserveReadingResult.Error -> Unit
                }
            }
        }
    }

    private fun refreshContinueReadingCards() {
        val ids = readingProgress.map { it.articleId }

        readingCardsJob?.cancel()
        readingCardsJob = viewModelScope.launch {
            val windowById = lastDomainArticles.associateBy { it.id }
            val missing = ids.filter { it !in windowById && it !in readingArticleCache }
            if (missing.isNotEmpty()) {
                getArticlesByIds(missing).forEach { readingArticleCache[it.id] = it }
            }

            val cards = ids.mapNotNull { id ->
                (windowById[id] ?: readingArticleCache[id])?.toCardUi()
            }
            _uiState.update { it.copy(continueReading = cards) }
        }
    }

    private fun flushPendingReading(uid: String) {
        viewModelScope.launch {
            val pending = pendingReadingStore.getOnce()
            if (pending.isEmpty()) return@launch
            pending.forEach { articleId ->
                upsertReadingProgress(uid = uid, articleId = articleId, progress = 0f)
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

    private suspend fun loadProfiles(uids: Collection<String>): Map<String, PublicUserProfile> {
        val missing = uids.distinct().filter { it !in profileCache }
        if (missing.isNotEmpty()) {
            userProfileRepository.getProfiles(missing).forEach { (uid, profile) ->
                profileCache[uid] = profile
            }
        }
        return uids.mapNotNull { profileCache[it] }.associateBy { it.uid }
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

    private fun PublicUserProfile?.displayNameOrFallback(): String =
        this?.name?.takeIf { it.isNotBlank() }?.let(::formatAuthorName) ?: FALLBACK_NAME

    private fun FavoriteAuthor.toAuthorUi(profile: PublicUserProfile?): AuthorUi = AuthorUi(
        uid = uid,
        name = profile.displayNameOrFallback(),
        photoUrl = profile?.photoUrl
    )
}