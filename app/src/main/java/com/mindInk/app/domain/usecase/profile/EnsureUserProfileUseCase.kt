package com.mindInk.app.domain.usecase.profile

import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class EnsureUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(user: AuthUser): Boolean = repository.ensureProfile(user)
}