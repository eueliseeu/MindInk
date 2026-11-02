package com.mindInk.app.ui.articles.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.data.reading.PendingReadingStore
import com.mindInk.app.domain.repository.RecordArticleViewResult
import com.mindInk.app.domain.repository.ToggleStarResult
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.RecordArticleViewUseCase
import com.mindInk.app.domain.usecase.article.ToggleStarUseCase
import com.mindInk.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.mindInk.app.domain.usecase.reading.MarkReadingCompletedUseCase
import com.mindInk.app.domain.usecase.reading.UpsertReadingProgressUseCase
import com.mindInk.app.domain.usecase.star.ObserveStarredArticleIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ArticleDetailVM"

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleById: GetArticleByIdUseCase,
    private val toggleStarUseCase: ToggleStarUseCase,
    private val recordArticleView: RecordArticleViewUseCase,
    private val observeAuthState: ObserveAuthStateUseCase,
    private val observeStarredArticleIds: ObserveStarredArticleIdsUseCase,
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val markReadingCompleted: MarkReadingCompletedUseCase,
    private val pendingReadingStore: PendingReadingStore
) : ViewModel() {

    private var articleId: String = ""
    private var viewRecordedKey: String? = null
    private var isScreenVisible: Boolean = false
    private var readingStarted: Boolean = false
    private var readingCompleted: Boolean = false

    private var starsJob: Job? = null
    private var starsUid: String? = null
    private var starredIds: Set<String> = emptySet()
    private var expectedStar: Boolean? = null
    private var starInFlight: Boolean = false

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthState().collect { user ->
                _uiState.update { it.copy(currentUid = user?.uid) }
                observeStars(user?.uid)
                tryRecordView()
                if (user != null) {
                    tryStartReading()
                }
            }
        }
    }

    fun onScreenVisible(visible: Boolean) {
        isScreenVisible = visible
        if (visible) {
            tryRecordView()
            tryStartReading()
        }
    }

    fun setArticleId(id: String) {
        if (articleId == id && _uiState.value.article != null) {
            tryRecordView()
            tryStartReading()
            return
        }
        articleId = id
        viewRecordedKey = null
        readingStarted = false
        readingCompleted = false
        expectedStar = null
        syncStarFlag()
        load()
    }

    fun load() {
        if (articleId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val article = getArticleById(articleId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    article = article,
                    errorMessage = if (article == null) "Artigo não encontrado." else null
                )
            }
            tryRecordView()
            tryStartReading()
        }
    }

    fun onScrollProgress(progress: Float) {
        if (readingCompleted) return
        if (progress >= 0.98f) {
            readingCompleted = true
            val uid = _uiState.value.currentUid ?: return
            if (articleId.isBlank()) return
            viewModelScope.launch {
                markReadingCompleted(uid, articleId)
            }
        }
    }

    fun onGuestContinueReadingClick() {
        if (articleId.isBlank()) return
        viewModelScope.launch {
            pendingReadingStore.add(articleId)
        }
    }

    private fun observeStars(uid: String?) {
        if (uid == starsUid) return
        starsUid = uid
        starsJob?.cancel()
        starredIds = emptySet()
        expectedStar = null
        syncStarFlag()
        if (uid == null) return

        starsJob = viewModelScope.launch {
            observeStarredArticleIds(uid).collect { ids ->
                starredIds = ids
                syncStarFlag()
            }
        }
    }

    private fun syncStarFlag() {
        val actual = articleId.isNotBlank() && articleId in starredIds
        if (expectedStar == actual) expectedStar = null
        val flag = expectedStar ?: actual
        _uiState.update { it.copy(isStarredByMe = flag) }
    }

    private fun tryStartReading() {
        if (readingStarted) return
        val uid = _uiState.value.currentUid ?: return
        val article = _uiState.value.article ?: return
        if (article.id != articleId) return
        if (!isScreenVisible) return

        readingStarted = true
        viewModelScope.launch {
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
    }

    private fun tryRecordView() {
        if (!isScreenVisible) return
        val uid = _uiState.value.currentUid ?: return
        if (articleId.isBlank()) return
        if (_uiState.value.article?.id != articleId) return

        val key = "$uid:$articleId"
        if (viewRecordedKey == key) return

        viewRecordedKey = key
        val targetArticleId = articleId
        viewModelScope.launch {
            when (val result = recordArticleView(targetArticleId, uid)) {
                is RecordArticleViewResult.Success -> {
                    _uiState.update { state ->
                        val current = state.article ?: return@update state
                        if (current.id != targetArticleId) return@update state
                        state.copy(
                            article = current.copy(viewCount = result.viewCount)
                        )
                    }
                }
                is RecordArticleViewResult.Error -> {
                    Log.e(TAG, "recordView falhou: ${result.message}")
                    if (viewRecordedKey == key) viewRecordedKey = null
                }
            }
        }
    }

    fun onToggleStar() {
        val state = _uiState.value
        val uid = state.currentUid ?: return
        val current = state.article ?: return
        if (articleId.isBlank() || starInFlight) return

        val targetArticleId = articleId
        val starred = !state.isStarredByMe
        val delta = if (starred) 1 else -1

        starInFlight = true
        expectedStar = starred
        _uiState.update {
            it.copy(
                isStarredByMe = starred,
                article = current.copy(starCount = (current.starCount + delta).coerceAtLeast(0))
            )
        }

        viewModelScope.launch {
            when (val result = toggleStarUseCase(targetArticleId, uid)) {
                is ToggleStarResult.Success -> {
                    val refreshed = getArticleById(targetArticleId)
                    if (refreshed != null && articleId == targetArticleId) {
                        _uiState.update { it.copy(article = refreshed) }
                    }
                }
                is ToggleStarResult.Error -> {
                    expectedStar = null
                    _uiState.update {
                        it.copy(
                            article = if (articleId == targetArticleId) current else it.article,
                            toastMessage = result.message
                        )
                    }
                    syncStarFlag()
                }
            }
            starInFlight = false
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}