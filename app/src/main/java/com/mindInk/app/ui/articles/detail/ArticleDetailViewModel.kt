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
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val upsertReadingProgress: UpsertReadingProgressUseCase,
    private val markReadingCompleted: MarkReadingCompletedUseCase,
    private val pendingReadingStore: PendingReadingStore
) : ViewModel() {

    private var articleId: String = ""
    private var viewRecordedKey: String? = null
    private var isScreenVisible: Boolean = false
    private var readingStarted: Boolean = false
    private var readingCompleted: Boolean = false

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthState().collect { user ->
                _uiState.update { it.copy(currentUid = user?.uid) }
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
                authorName = article.authorName,
                authorPhotoUrl = article.authorPhotoUrl,
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
        val uid = _uiState.value.currentUid ?: return
        val current = _uiState.value.article ?: return
        if (articleId.isBlank()) return

        val starred = current.starredBy.contains(uid)
        val optimistic = if (starred) {
            current.copy(
                starredBy = current.starredBy - uid,
                starCount = (current.starCount - 1).coerceAtLeast(0)
            )
        } else {
            current.copy(
                starredBy = current.starredBy + uid,
                starCount = current.starCount + 1
            )
        }
        _uiState.update { it.copy(article = optimistic) }

        viewModelScope.launch {
            when (val result = toggleStarUseCase(articleId, uid)) {
                is ToggleStarResult.Success -> {
                    val refreshed = getArticleById(articleId)
                    if (refreshed != null) {
                        _uiState.update { it.copy(article = refreshed) }
                    }
                }
                is ToggleStarResult.Error -> {
                    _uiState.update {
                        it.copy(
                            article = current,
                            toastMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}