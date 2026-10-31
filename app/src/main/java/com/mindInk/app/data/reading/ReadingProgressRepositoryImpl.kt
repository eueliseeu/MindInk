package com.mindInk.app.data.reading

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

private const val ROOT = "user_reading"
private const val ITEMS = "items"

@Singleton
class ReadingProgressRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReadingProgressRepository {

    private fun items(uid: String) =
        firestore.collection(ROOT).document(uid).collection(ITEMS)

    override fun observeContinueReading(
        uid: String,
        limit: Long
    ): Flow<ObserveReadingResult> = callbackFlow {
        val registration = items(uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        ObserveReadingResult.Error(
                            error.message ?: "Falha ao carregar Continue Lendo."
                        )
                    )
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val dto = doc.toObject(ReadingProgressDto::class.java)
                            ?: return@mapNotNull null
                        if (dto.articleId.isBlank() || dto.title.isBlank()) {
                            return@mapNotNull null
                        }
                        if (dto.progress >= 1f) return@mapNotNull null
                        ReadingProgress(
                            articleId = dto.articleId,
                            title = dto.title,
                            category = dto.category,
                            authorName = dto.authorName,
                            authorPhotoUrl = dto.authorPhotoUrl,
                            progress = dto.progress,
                            updatedAt = dto.updatedAt
                        )
                    }
                    ?.take(limit.toInt())
                    .orEmpty()
                trySend(ObserveReadingResult.Success(list))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertProgress(
        uid: String,
        articleId: String,
        title: String,
        category: String,
        authorName: String,
        authorPhotoUrl: String?,
        progress: Float
    ): UpsertReadingResult = try {
        val clamped = progress.coerceIn(0f, 1f)
        items(uid).document(articleId).set(
            mapOf(
                "articleId" to articleId,
                "title" to title,
                "category" to category,
                "authorName" to authorName,
                "authorPhotoUrl" to authorPhotoUrl,
                "progress" to clamped,
                "updatedAt" to System.currentTimeMillis()
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
        items(uid).document(articleId).set(
            mapOf(
                "progress" to 1f,
                "updatedAt" to System.currentTimeMillis()
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