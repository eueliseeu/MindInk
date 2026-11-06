package com.mindInk.app.ui.articles.editor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.MAX_ARTICLE_CHARS
import com.mindInk.app.core.MAX_ARTICLE_WORDS
import com.mindInk.app.core.countWords
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.CreateArticleResult
import com.mindInk.app.domain.repository.UpdateArticleResult
import com.mindInk.app.domain.usecase.article.CreateArticleUseCase
import com.mindInk.app.domain.usecase.article.GetArticleByIdUseCase
import com.mindInk.app.domain.usecase.article.GetArticleContentUseCase
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
private const val LIMIT_MESSAGE = "Limite de 10.000 palavras atingido."

@HiltViewModel
class CreateEditArticleViewModel @Inject constructor(
    private val createArticle: CreateArticleUseCase,
    private val updateArticle: UpdateArticleUseCase,
    private val getArticleById: GetArticleByIdUseCase,
    private val getArticleContent: GetArticleContentUseCase
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

    fun loadForEdit(user: AuthUser, articleId: String) {
        boundUser = user
        if (articleId.isBlank()) {
            resetForNewArticle(user)
            return
        }
        if (_uiState.value.isEditMode &&
            _uiState.value.articleId == articleId &&
            !_uiState.value.isLoading &&
            _uiState.value.title.isNotBlank()
        ) {
            bindAuthor(user)
            return
        }
        viewModelScope.launch {
            _uiState.value = CreateEditArticleUiState(
                articleId = articleId,
                isEditMode = true,
                isLoading = true,
                authorLabel = formatAuthorName(user.displayName ?: user.email)
            )
            val article = getArticleById(articleId)
            if (article == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Artigo não encontrado."
                    )
                }
                return@launch
            }
            if (article.authorUid != user.uid) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sem permissão para editar este artigo."
                    )
                }
                return@launch
            }
            val body = getArticleContent(articleId).orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    category = article.category,
                    title = article.title,
                    subtitle = article.subtitle,
                    content = TextFieldValue(body),
                    errorMessage = null
                )
            }
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(category = category, errorMessage = null) }
    }

    fun onTitleChange(value: String) {
        if (value.length <= TITLE_MAX) {
            _uiState.update { it.copy(title = value.replace('\n', ' '), errorMessage = null) }
        }
    }

    fun onSubtitleChange(value: String) {
        if (value.length <= SUBTITLE_MAX) {
            _uiState.update { it.copy(subtitle = value.replace('\n', ' '), errorMessage = null) }
        }
    }

    fun onContentChange(value: TextFieldValue) {
        val current = _uiState.value.content
        val sanitized = MarkdownFormatter.sanitize(value)

        if (sanitized.text == current.text) {
            _uiState.update { it.copy(content = sanitized) }
            return
        }

        updateContent(MarkdownFormatter.continueList(current, sanitized))
    }

    fun onPreviewChange(enabled: Boolean) {
        _uiState.update { it.copy(isPreview = enabled) }
    }

    fun applyBold() = format { MarkdownFormatter.wrap(it, "**", "**", "texto em negrito") }

    fun applyItalic() = format { MarkdownFormatter.wrap(it, "_", "_", "texto em itálico") }

    fun applyHeading() = format(MarkdownFormatter::heading)

    fun applyBullet() = format(MarkdownFormatter::bullet)

    fun applyNumbered() = format(MarkdownFormatter::numbered)

    fun applyQuote() = format(MarkdownFormatter::quote)

    fun applyLink() = format(MarkdownFormatter::link)

    fun applyCode() = format(MarkdownFormatter::code)

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
        if (state.content.text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escreva o conteúdo em Markdown.") }
            return
        }
        if (exceedsLimit(state.content.text)) {
            _uiState.update { it.copy(errorMessage = LIMIT_MESSAGE) }
            return
        }
        if (state.isSaving) return

        val authorName = formatAuthorName(user.displayName)

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
                        content = state.content.text
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
                        authorPhotoUrl = user.photoUrl,
                        category = state.category,
                        title = state.title,
                        subtitle = state.subtitle,
                        content = state.content.text
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

    private fun format(transform: (TextFieldValue) -> TextFieldValue) {
        updateContent(transform(_uiState.value.content))
    }

    private fun updateContent(next: TextFieldValue) {
        if (exceedsLimit(next.text)) {
            _uiState.update { it.copy(errorMessage = LIMIT_MESSAGE) }
            return
        }
        _uiState.update { it.copy(content = next, errorMessage = null) }
    }

    private fun exceedsLimit(text: String): Boolean =
        text.length > MAX_ARTICLE_CHARS || countWords(text) > MAX_ARTICLE_WORDS
}