package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.StarRepository
import javax.inject.Inject

class ToggleStarUseCase @Inject constructor(
    private val repository: StarRepository
) {
    suspend operator fun invoke(articleId: String, uid: String) =
        repository.toggleStar(articleId, uid)
}