package com.mindInk.app.data.favorite

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mindInk.app.domain.model.FavoriteAuthor
import com.mindInk.app.domain.repository.FavoriteAuthorRepository
import com.mindInk.app.domain.repository.ObserveFavoriteAuthorsResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteAuthorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FavoriteAuthorRepository {

    override fun observeFavoriteAuthors(
        uid: String,
        limit: Long
    ): Flow<ObserveFavoriteAuthorsResult> = callbackFlow {
        val registration = firestore.favoriteAuthors(uid)
            .orderBy(FIELD_FAVORITED_AT, Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        ObserveFavoriteAuthorsResult.Error(
                            error.message ?: "Falha ao carregar autores favoritos."
                        )
                    )
                    return@addSnapshotListener
                }
                val authors = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(FavoriteAuthorDto::class.java)?.let { dto ->
                            FavoriteAuthor(
                                uid = doc.id,
                                name = dto.authorName,
                                photoUrl = dto.authorPhotoUrl,
                                favoritedAt = dto.favoritedAt
                            )
                        }
                    }
                    .orEmpty()
                trySend(ObserveFavoriteAuthorsResult.Success(authors))
            }
        awaitClose { registration.remove() }
    }
}