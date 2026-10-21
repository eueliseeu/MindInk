package com.mindInk.app.data.article

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.mindInk.app.domain.model.Article
import com.mindInk.app.domain.repository.ArticleRepository
import com.mindInk.app.domain.repository.ArticlesPage
import com.mindInk.app.domain.repository.CreateArticleResult
import com.mindInk.app.domain.repository.DeleteArticleResult
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.repository.ToggleStarResult
import com.mindInk.app.domain.repository.UpdateArticleResult
import com.mindInk.app.domain.repository.UpdateAuthorArticlesResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION = "articles"
private const val AUTHOR_NAME_BATCH_SIZE = 450L

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ArticleRepository {

    private val articles = firestore.collection(COLLECTION)

    override fun observeArticles(limit: Long): Flow<ObserveArticlesResult> =
        observeArticlesQuery(
            articles.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
        )

    override fun observeAllArticles(): Flow<ObserveArticlesResult> =
        observeArticlesQuery(articles.orderBy("createdAt", Query.Direction.DESCENDING))

    private fun observeArticlesQuery(query: Query): Flow<ObserveArticlesResult> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(
                    ObserveArticlesResult.Error(
                        error.message ?: "Falha ao carregar artigos."
                    )
                )
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { it.toArticle() }
                .orEmpty()
            trySend(ObserveArticlesResult.Success(list))
        }
        awaitClose { registration.remove() }
    }

    override fun observeArticlesByAuthor(
        authorUid: String,
        limit: Long
    ): Flow<ObserveArticlesResult> = callbackFlow {
        val registration = articles
            .whereEqualTo("authorUid", authorUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        ObserveArticlesResult.Error(
                            error.message ?: "Falha ao carregar artigos do autor."
                        )
                    )
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toArticle() }
                    .orEmpty()
                trySend(ObserveArticlesResult.Success(list))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun loadArticlesPage(
        pageSize: Long,
        startAfterDocumentId: String?
    ): LoadArticlesPageResult {
        return try {
            var query: Query = articles
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize)

            if (!startAfterDocumentId.isNullOrBlank()) {
                val cursorSnap = articles.document(startAfterDocumentId).get().await()
                if (cursorSnap.exists()) {
                    query = query.startAfter(cursorSnap)
                }
            }

            val snapshot = query.get().await()
            val list = snapshot.documents.mapNotNull { it.toArticle() }
            val lastId = snapshot.documents.lastOrNull()?.id

            LoadArticlesPageResult.Success(
                ArticlesPage(
                    articles = list,
                    nextCursorId = lastId,
                    hasMore = snapshot.documents.size >= pageSize.toInt()
                )
            )
        } catch (e: Exception) {
            LoadArticlesPageResult.Error(
                e.message ?: "Falha ao carregar mais artigos."
            )
        }
    }

    override suspend fun getArticleById(articleId: String): Article? {
        return try {
            articles.document(articleId).get().await().toArticle()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createArticle(
        authorUid: String,
        authorName: String,
        authorUsername: String,
        authorPhotoUrl: String?,
        category: String,
        title: String,
        subtitle: String,
        content: String,
        linkUrl: String
    ): CreateArticleResult {
        return try {
            val now = System.currentTimeMillis()
            val ref = articles.document()
            val dto = ArticleDto(
                authorUid = authorUid,
                authorName = authorName,
                authorUsername = authorUsername,
                authorPhotoUrl = authorPhotoUrl,
                category = category,
                title = title,
                subtitle = subtitle,
                content = content,
                linkUrl = linkUrl,
                starredBy = emptyList(),
                starCount = 0,
                createdAt = now,
                updatedAt = now
            )
            ref.set(dto).await()
            CreateArticleResult.Success(ref.id)
        } catch (e: Exception) {
            CreateArticleResult.Error(e.message ?: "Não foi possível publicar o artigo.")
        }
    }

    override suspend fun updateArticle(
        articleId: String,
        authorUid: String,
        category: String,
        title: String,
        subtitle: String,
        content: String,
        linkUrl: String
    ): UpdateArticleResult {
        return try {
            val ref = articles.document(articleId)
            val existing = ref.get().await().toArticle()
                ?: return UpdateArticleResult.Error("Artigo não encontrado.")
            if (existing.authorUid != authorUid) {
                return UpdateArticleResult.Error("Sem permissão para editar este artigo.")
            }
            ref.set(
                mapOf(
                    "category" to category,
                    "title" to title,
                    "subtitle" to subtitle,
                    "content" to content,
                    "linkUrl" to linkUrl,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
            UpdateArticleResult.Success
        } catch (e: Exception) {
            UpdateArticleResult.Error(e.message ?: "Não foi possível atualizar o artigo.")
        }
    }

    override suspend fun updateAuthorName(
        authorUid: String,
        authorName: String
    ): UpdateAuthorArticlesResult = try {
        val query = articles
            .whereEqualTo("authorUid", authorUid)
            .orderBy(FieldPath.documentId())
        var page = query.limit(AUTHOR_NAME_BATCH_SIZE).get().await()

        while (!page.isEmpty) {
            val batch = firestore.batch()
            page.documents.forEach { document ->
                batch.update(document.reference, "authorName", authorName)
            }
            batch.commit().await()
            page = query
                .startAfter(page.documents.last())
                .limit(AUTHOR_NAME_BATCH_SIZE)
                .get()
                .await()
        }

        UpdateAuthorArticlesResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        UpdateAuthorArticlesResult.Error(
            e.message ?: "Não foi possível atualizar o nome em todos os artigos."
        )
    }

    override suspend fun deleteArticle(
        articleId: String,
        requesterUid: String
    ): DeleteArticleResult {
        return try {
            val ref = articles.document(articleId)
            val existing = ref.get().await().toArticle()
                ?: return DeleteArticleResult.Error("Artigo não encontrado.")
            if (existing.authorUid != requesterUid) {
                return DeleteArticleResult.Error("Sem permissão para apagar este artigo.")
            }
            ref.delete().await()
            DeleteArticleResult.Success
        } catch (e: Exception) {
            DeleteArticleResult.Error(e.message ?: "Não foi possível apagar o artigo.")
        }
    }

    override suspend fun toggleStar(
        articleId: String,
        uid: String
    ): ToggleStarResult {
        return try {
            firestore.runTransaction { transaction ->
                val ref = articles.document(articleId)
                val snap = transaction.get(ref)
                val starredBy = (snap.get("starredBy") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList()
                    ?: mutableListOf()
                if (starredBy.contains(uid)) {
                    starredBy.remove(uid)
                } else {
                    starredBy.add(uid)
                }
                transaction.update(
                    ref,
                    mapOf(
                        "starredBy" to starredBy,
                        "starCount" to starredBy.size
                    )
                )
            }.await()
            ToggleStarResult.Success
        } catch (e: Exception) {
            ToggleStarResult.Error(e.message ?: "Não foi possível atualizar a estrela.")
        }
    }
}
