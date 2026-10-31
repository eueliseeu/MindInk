package com.mindInk.app.domain.usecase.reading

import com.mindInk.app.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class ObserveContinueReadingUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    operator fun invoke(uid: String, limit: Long = 5) =
        repository.observeContinueReading(uid, limit)
}