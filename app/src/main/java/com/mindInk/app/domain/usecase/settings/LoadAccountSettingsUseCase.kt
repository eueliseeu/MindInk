package com.mindInk.app.domain.usecase.settings

import com.mindInk.app.domain.repository.AccountSettingsRepository
import javax.inject.Inject

class LoadAccountSettingsUseCase @Inject constructor(
    private val repository: AccountSettingsRepository
) {
    suspend operator fun invoke(uid: String) = repository.load(uid)
}