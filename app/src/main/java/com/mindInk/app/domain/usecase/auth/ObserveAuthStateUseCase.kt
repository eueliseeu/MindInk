package com.mindInk.app.domain.usecase.auth

import com.mindInk.app.domain.repository.AuthRepository
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.observeAuthState()
}
