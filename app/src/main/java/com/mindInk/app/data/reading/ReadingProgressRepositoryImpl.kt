package com.mindInk.app.data.reading

import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.mindInk.app.domain.model.ReadingProgress
import com.mindInk.app.domain.repository.ObserveReadingResult
import com.mindInk.app.domain.repository.ReadingProgressRepository
import com.mindInk.app.domain.repository.UpsertReadingResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReadingProgressRepository {

    override fun observeContinueReading(
        uid: String,
        limit: Long
    ): Flow<ObserveReadingResult> = callbackFlow {
        val registration = firestore.readingProgress(uid)
            .whereEqualTo(READING_FIELD_COMPLETED, false)
            .orderBy(READING_FIELD_UPDATED_AT, Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        ObserveReadingResult.Error(
                            error.message ?: "Falha ao carregar Continue Lendo."
                        )
                    )
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.map { doc ->
                        ReadingProgress(
                            articleId = doc.id,
                            progress = (doc.getDouble(READING_FIELD_PROGRESS) ?: 0.0).toFloat(),
                            updatedAt = doc
                                .getTimestamp(READING_FIELD_UPDATED_AT, ServerTimestampBehavior.ESTIMATE)
                                ?.toDate()
                                ?.time
                                ?: 0L
                        )
                    }
                    .orEmpty()
                trySend(ObserveReadingResult.Success(items))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertProgress(
        uid: String,
        articleId: String,
        progress: Float
    ): UpsertReadingResult = try {
        val clamped = progress.coerceIn(0f, 1f)
        firestore.readingProgress(uid).document(articleId).set(
            mapOf(
                READING_FIELD_ARTICLE_ID to articleId,
                READING_FIELD_PROGRESS to clamped,
                READING_FIELD_COMPLETED to (clamped >= 1f),
                READING_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        UpsertReadingResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        UpsertReadingResult.Error(
            e.message ?: "Não foi possível salvar o progresso."
        )
    }

    override suspend fun markCompleted(
        uid: String,
        articleId: String
    ): UpsertReadingResult = try {
        firestore.readingProgress(uid).document(articleId).set(
            mapOf(
                READING_FIELD_ARTICLE_ID to articleId,
                READING_FIELD_PROGRESS to 1f,
                READING_FIELD_COMPLETED to true,
                READING_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        UpsertReadingResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        UpsertReadingResult.Error(
            e.message ?: "Não foi possível marcar como lido."
        )
    }
}