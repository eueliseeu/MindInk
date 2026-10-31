package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.AccountSettings

sealed interface LoadAccountSettingsResult {
    data class Success(val settings: AccountSettings) : LoadAccountSettingsResult
    data class Error(val message: String) : LoadAccountSettingsResult
}

sealed interface SaveAccountSettingsResult {
    data object Success : SaveAccountSettingsResult
    data class Error(val message: String) : SaveAccountSettingsResult
}

interface AccountSettingsRepository {
    suspend fun load(uid: String): LoadAccountSettingsResult

    suspend fun save(uid: String, settings: AccountSettings): SaveAccountSettingsResult
}