package com.mindInk.app.data.star

import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mindInk.app.data.article.ARTICLES_COLLECTION
import com.mindInk.app.data.favorite.applyStarToFavorite
import com.mindInk.app.data.favorite.favoriteAuthors
import com.mindInk.app.domain.model.ReceivedStar
import com.mindInk.app.domain.repository.ObserveReceivedStarsResult
import com.mindInk.app.domain.repository.StarRepository
import com.mindInk.app.domain.repository.ToggleStarResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StarRepository {

    override fun observeStarredArticleIds(uid: String): Flow<Set<String>> = callbackFlow {
        val registration = firestore.stars(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                trySend(snapshot.documents.mapTo(HashSet()) { it.id })
            }
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    override fun observeReceivedStars(
        authorUid: String,
        limit: Long
    ): Flow<ObserveReceivedStarsResult> = callbackFlow {
        val registration = firestore.collectionGroup(STARS_COLLECTION)
            .whereEqualTo(STAR_FIELD_AUTHOR_UID, authorUid)
            .orderBy(STAR_FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        ObserveReceivedStarsResult.Error(
                            error.message ?: "Falha ao carregar estrelas recebidas."
                        )
                    )
                    return@addSnapshotListener
                }
                val stars = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val starerUid = doc.reference.parent.parent?.id
                            ?: return@mapNotNull null
                        ReceivedStar(
                            articleId = doc.id,
                            starerUid = starerUid,
                            createdAt = doc
                                .getTimestamp(STAR_FIELD_CREATED_AT, ServerTimestampBehavior.ESTIMATE)
                                ?.toDate()
                                ?.time
                                ?: 0L
                        )
                    }
                    .orEmpty()
                trySend(ObserveReceivedStarsResult.Success(stars))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun toggleStar(
        articleId: String,
        uid: String
    ): ToggleStarResult = try {
        firestore.runTransaction { transaction ->
            val articleRef = firestore.collection(ARTICLES_COLLECTION).document(articleId)
            val starRef = firestore.stars(uid).document(articleId)

            val article = transaction.get(articleRef)
            check(article.exists()) { "Artigo não encontrado." }
            val star = transaction.get(starRef)

            val authorUid = article.getString("authorUid").orEmpty()
            val favorite = authorUid
                .takeIf { it.isNotBlank() && it != uid }
                ?.let { id ->
                    val ref = firestore.favoriteAuthors(uid).document(id)
                    ref to transaction.get(ref)
                }

            val starred = !star.exists()
            val count = (article.getLong("starCount") ?: 0L).toInt()

            if (starred) {
                transaction.set(
                    starRef,
                    mapOf(
                        STAR_FIELD_ARTICLE_ID to articleId,
                        STAR_FIELD_AUTHOR_UID to authorUid,
                        STAR_FIELD_CREATED_AT to FieldValue.serverTimestamp()
                    )
                )
                transaction.update(articleRef, "starCount", count + 1)
            } else {
                transaction.delete(starRef)
                transaction.update(articleRef, "starCount", (count - 1).coerceAtLeast(0))
            }

            favorite?.let { (ref, snapshot) ->
                transaction.applyStarToFavorite(
                    favoriteRef = ref,
                    favorite = snapshot,
                    article = article,
                    starred = starred,
                    now = System.currentTimeMillis()
                )
            }
        }.await()
        ToggleStarResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ToggleStarResult.Error(e.message ?: "Não foi possível atualizar a estrela.")
    }
}