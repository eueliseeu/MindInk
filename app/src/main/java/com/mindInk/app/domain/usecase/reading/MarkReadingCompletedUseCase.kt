package com.mindInk.app.domain.usecase.reading

import com.mindInk.app.domain.repository.ReadingProgressRepository
import com.mindInk.app.domain.repository.UpsertReadingResult
import javax.inject.Inject

class MarkReadingCompletedUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(
        uid: String,
        articleId: String
    ): UpsertReadingResult = repository.markCompleted(uid, articleId)
}