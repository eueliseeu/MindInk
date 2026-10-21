package com.mindInk.app.domain.usecase.auth

import com.mindInk.app.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateDisplayNameUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String) = repository.updateDisplayName(name)
}
