package com.mindInk.app.domain.usecase.settings

import com.mindInk.app.domain.model.AccountSettings
import com.mindInk.app.domain.repository.AccountSettingsRepository
import javax.inject.Inject

class SaveAccountSettingsUseCase @Inject constructor(
    private val repository: AccountSettingsRepository
) {
    suspend operator fun invoke(uid: String, settings: AccountSettings) =
        repository.save(uid, settings)
}
