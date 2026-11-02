package com.mindInk.app.domain.usecase.star

import com.mindInk.app.domain.repository.StarRepository
import javax.inject.Inject

class ObserveReceivedStarsUseCase @Inject constructor(
    private val repository: StarRepository
) {
    operator fun invoke(authorUid: String, limit: Long = 10) =
        repository.observeReceivedStars(authorUid, limit)
}