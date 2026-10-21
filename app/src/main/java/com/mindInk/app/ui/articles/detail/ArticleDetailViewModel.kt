package com.mindInk.app.ui.articles.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.domain.repository.RecordArticleViewResult
import com.mindInk.app.domain.repository.ToggleStarResult
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.RecordArticleViewUseCase
import com.mindInk.app.domain.usecase.article.ToggleStarUseCase
import com.mindInk.app.domain.usecase.auth.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleById: GetArticleByIdUseCase,
    private val toggleStarUseCase: ToggleStarUseCase,
    private val recordArticleView: RecordArticleViewUseCase,
    private val observeAuthState: ObserveAuthStateUseCase
) : ViewModel() {

    private var articleId: String = ""
    private var viewRecordedForId: String? = null

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthState().collect { user ->
                _uiState.update { it.copy(currentUid = user?.uid) }
                tryRecordView()
            }
        }
    }

    fun setArticleId(id: String) {
        if (articleId == id && _uiState.value.article != null) return
        articleId = id
        viewRecordedForId = null
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
        }
    }

    private fun tryRecordView() {
        val uid = _uiState.value.currentUid ?: return
        if (articleId.isBlank()) return
        if (viewRecordedForId == articleId) return
        if (_uiState.value.article == null) return

        viewRecordedForId = articleId
        viewModelScope.launch {
            when (val result = recordArticleView(articleId, uid)) {
                is RecordArticleViewResult.Success -> {
                    _uiState.update { state ->
                        val current = state.article ?: return@update state
                        state.copy(
                            article = current.copy(viewCount = result.viewCount)
                        )
                    }
                }
                is RecordArticleViewResult.Error -> {
                    viewRecordedForId = null
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
