package com.mindInk.app.domain.usecase.star

import com.mindInk.app.domain.repository.StarRepository
import javax.inject.Inject

class ObserveStarredArticleIdsUseCase @Inject constructor(
    private val repository: StarRepository
) {
    operator fun invoke(uid: String) = repository.observeStarredArticleIds(uid)
}