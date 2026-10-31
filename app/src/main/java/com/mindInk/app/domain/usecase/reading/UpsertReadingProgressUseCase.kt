package com.mindInk.app.domain.usecase.reading

import com.mindInk.app.domain.repository.ReadingProgressRepository
import com.mindInk.app.domain.repository.UpsertReadingResult
import javax.inject.Inject

class UpsertReadingProgressUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(
        uid: String,
        articleId: String,
        title: String,
        category: String,
        authorName: String,
        authorPhotoUrl: String?,
        progress: Float
    ): UpsertReadingResult = repository.upsertProgress(
        uid = uid,
        articleId = articleId,
        title = title,
        category = category,
        authorName = authorName,
        authorPhotoUrl = authorPhotoUrl,
        progress = progress
    )
}