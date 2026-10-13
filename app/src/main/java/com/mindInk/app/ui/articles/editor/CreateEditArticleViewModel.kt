package com.mindInk.app.ui.articles.editor

import androidx.lifecycle.ViewModel
import com.mindInk.app.domain.model.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val TITLE_MAX = 120
private const val SUBTITLE_MAX = 200
private const val CONTENT_MAX = 50_000

@HiltViewModel
class CreateEditArticleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditArticleUiState())
    val uiState: StateFlow<CreateEditArticleUiState> = _uiState.asStateFlow()

    fun bindAuthor(user: AuthUser) {
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "Autor"
        _uiState.update { it.copy(authorLabel = name) }
    }

    fun onTitleChange(value: String) {
        if (value.length <= TITLE_MAX) {
            _uiState.update { it.copy(title = value, errorMessage = null) }
        }
    }

    fun onSubtitleChange(value: String) {
        if (value.length <= SUBTITLE_MAX) {
            _uiState.update { it.copy(subtitle = value, errorMessage = null) }
        }
    }

    fun onContentChange(value: String) {
        if (value.length <= CONTENT_MAX) {
            _uiState.update { it.copy(content = value, errorMessage = null) }
        }
    }

    fun togglePreview() {
        _uiState.update { it.copy(isPreview = !it.isPreview) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe o título.") }
            return
        }
        if (state.content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escreva o conteúdo em Markdown.") }
            return
        }
        _uiState.update {
            it.copy(
                isSaving = false,
                saved = true,
                errorMessage = null
            )
        }
    }
}
