package com.mindInk.app.ui.articles.editor

data class CreateEditArticleUiState(
    val articleId: String? = null,
    val isEditMode: Boolean = false,
    val authorLabel: String = "",
    val title: String = "",
    val subtitle: String = "",
    val content: String = "",
    val isPreview: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)
