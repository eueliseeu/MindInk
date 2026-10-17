package com.mindInk.app.ui.articles.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.CreateArticleResult
import com.mindInk.app.domain.repository.UpdateArticleResult
import com.mindInk.app.domain.usecase.article.CreateArticleUseCase
import com.mindInk.app.domain.usecase.article.UpdateArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TITLE_MAX = 120
private const val SUBTITLE_MAX = 200
private const val CONTENT_MAX = 50_000

@HiltViewModel
class CreateEditArticleViewModel @Inject constructor(
    private val createArticle: CreateArticleUseCase,
    private val updateArticle: UpdateArticleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditArticleUiState())
    val uiState: StateFlow<CreateEditArticleUiState> = _uiState.asStateFlow()

    private var boundUser: AuthUser? = null

    fun resetForNewArticle(user: AuthUser) {
        boundUser = user
        _uiState.value = CreateEditArticleUiState(
            authorLabel = formatAuthorName(user.displayName ?: user.email)
        )
    }

    fun bindAuthor(user: AuthUser) {
        boundUser = user
        _uiState.update {
            it.copy(authorLabel = formatAuthorName(user.displayName ?: user.email))
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(category = category, errorMessage = null) }
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

    fun insertMarkdown(snippet: String) {
        val current = _uiState.value.content
        val separator = if (current.isBlank() || current.endsWith("\n")) "" else "\n"
        val next = (current + separator + snippet).take(CONTENT_MAX)
        _uiState.update { it.copy(content = next, isPreview = false, errorMessage = null) }
    }

    fun applyBold() = insertMarkdown("**texto em negrito**")
    fun applyItalic() = insertMarkdown("*texto em itálico*")
    fun applyHeading() = insertMarkdown("\n## Título da seção\n")
    fun applyBullet() = insertMarkdown("- item da lista\n")
    fun applyLink() = insertMarkdown("[texto do link](https://)")
    fun applyCode() = insertMarkdown("\n```\ncódigo\n```\n")
    fun applyImage() = insertMarkdown("![descrição](https://url-da-imagem)")

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    fun save() {
        val user = boundUser
        if (user == null) {
            _uiState.update { it.copy(errorMessage = "Sessão inválida. Faça login novamente.") }
            return
        }
        val state = _uiState.value
        if (state.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selecione a categoria do artigo.") }
            return
        }
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe o título.") }
            return
        }
        if (state.content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escreva o conteúdo em Markdown.") }
            return
        }
        if (state.isSaving) return

        val authorName = formatAuthorName(
            user.displayName ?: user.email?.substringBefore("@")
        )
        val authorUsername = user.email?.substringBefore("@")
            ?: user.uid.take(8)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            if (state.isEditMode && !state.articleId.isNullOrBlank()) {
                when (
                    val result = updateArticle(
                        articleId = state.articleId,
                        authorUid = user.uid,
                        category = state.category,
                        title = state.title,
                        subtitle = state.subtitle,
                        content = state.content
                    )
                ) {
                    is UpdateArticleResult.Success -> {
                        _uiState.update {
                            it.copy(isSaving = false, saved = true, errorMessage = null)
                        }
                    }
                    is UpdateArticleResult.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = result.message)
                        }
                    }
                }
            } else {
                when (
                    val result = createArticle(
                        authorUid = user.uid,
                        authorName = authorName,
                        authorUsername = authorUsername,
                        authorPhotoUrl = user.photoUrl,
                        category = state.category,
                        title = state.title,
                        subtitle = state.subtitle,
                        content = state.content
                    )
                ) {
                    is CreateArticleResult.Success -> {
                        val name = _uiState.value.authorLabel
                        _uiState.value = CreateEditArticleUiState(
                            authorLabel = name,
                            saved = true
                        )
                    }
                    is CreateArticleResult.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }
}
