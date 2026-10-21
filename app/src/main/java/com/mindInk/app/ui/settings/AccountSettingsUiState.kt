package com.mindInk.app.ui.settings

data class AccountSettingsUiState(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val github: String = "",
    val instagram: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val accountDeleted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
