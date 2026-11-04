package com.mindInk.app.ui.articles.editor

import androidx.compose.ui.text.input.TextFieldValue

data class CreateEditArticleUiState(
    val articleId: String? = null,
    val isEditMode: Boolean = false,
    val authorLabel: String = "",
    val category: String = "",
    val title: String = "",
    val subtitle: String = "",
    val content: TextFieldValue = TextFieldValue(),
    val isPreview: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)