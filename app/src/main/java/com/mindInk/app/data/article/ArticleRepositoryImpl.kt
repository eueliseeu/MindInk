package com.mindInk.app.data.article

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mindInk.app.core.buildExcerpt
import com.mindInk.app.domain.model.Article
import com.mindInk.app.domain.repository.ArticleRepository
import com.mindInk.app.domain.repository.ArticlesPage
import com.mindInk.app.domain.repository.CreateArticleResult
import com.mindInk.app.domain.repository.DeleteArticleResult
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.repository.RecordArticleViewResult
import com.mindInk.app.domain.repository.UpdateArticleResult
import com.mindInk.app.domain.repository.UpdateAuthorArticlesResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTHOR_NAME_BATCH_SIZE = 450L
private const val IN_QUERY_LIMIT = 10

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ArticleRepository {

    private val articles = firestore.collection(ARTICLES_COLLECTION)

    override fun observeArticles(limit: Long): Flow<ObserveArticlesResult> =
        observeArticlesQuery(
            query = articles.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit),
            errorMessage = "Falha ao carregar artigos."
        )

    override fun observeArticlesByAuthor(
        authorUid: String,
        limit: Long
    ): Flow<ObserveArticlesResult> =
        observeArticlesQuery(
            query = articles
                .whereEqualTo("authorUid", authorUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit),
            errorMessage = "Falha ao carregar artigos do autor."
        )

    override fun observeArticlesByAuthors(
        authorUids: List<String>,
        limit: Long
    ): Flow<ObserveArticlesResult> {
        val uids = authorUids.filter { it.isNotBlank() }.distinct().take(IN_QUERY_LIMIT)
        if (uids.isEmpty()) return flowOf(ObserveArticlesResult.Success(emptyList()))

        return observeArticlesQuery(
            query = articles
                .whereIn("authorUid", uids)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit),
            errorMessage = "Falha ao carregar artigos dos autores."
        )
    }

    private fun observeArticlesQuery(
        query: Query,
        errorMessage: String
    ): Flow<ObserveArticlesResult> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ObserveArticlesResult.Error(error.message ?: errorMessage))
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadArticlesPageResult.Error(
                e.message ?: "Falha ao carregar mais artigos."
            )
        }
    }

    override suspend fun getArticleById(articleId: String): Article? {
        return try {
            articles.document(articleId).get().await().toArticle()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getArticlesByIds(articleIds: Collection<String>): List<Article> {
        val ids = articleIds.filter { it.isNotBlank() }.distinct()
        if (ids.isEmpty()) return emptyList()

        return try {
            ids.chunked(IN_QUERY_LIMIT).flatMap { chunk ->
                articles
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toArticle() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getArticleContent(articleId: String): String? {
        return try {
            firestore.articleContentRef(articleId)
                .get()
                .await()
                .getString(ARTICLE_CONTENT_FIELD)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createArticle(
        authorUid: String,
        authorName: String,
        authorPhotoUrl: String?,
        category: String,
        title: String,
        subtitle: String,
        content: String
    ): CreateArticleResult {
        return try {
            val ref = articles.document()
            val batch = firestore.batch()
            batch.set(
                ref,
                mapOf(
                    "authorUid" to authorUid,
                    "authorName" to authorName,
                    "authorPhotoUrl" to authorPhotoUrl,
                    "category" to category,
                    "title" to title,
                    "subtitle" to subtitle,
                    "excerpt" to buildExcerpt(content),
                    "starCount" to 0,
                    "viewCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                firestore.articleContentRef(ref.id),
                mapOf(ARTICLE_CONTENT_FIELD to content)
            )
            batch.commit().await()
            CreateArticleResult.Success(ref.id)
        } catch (e: CancellationException) {
            throw e
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
        content: String
    ): UpdateArticleResult {
        return try {
            val ref = articles.document(articleId)
            val existing = ref.get().await().toArticle()
                ?: return UpdateArticleResult.Error("Artigo não encontrado.")
            if (existing.authorUid != authorUid) {
                return UpdateArticleResult.Error("Sem permissão para editar este artigo.")
            }

            val batch = firestore.batch()
            batch.update(
                ref,
                mapOf(
                    "category" to category,
                    "title" to title,
                    "subtitle" to subtitle,
                    "excerpt" to buildExcerpt(content),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                firestore.articleContentRef(articleId),
                mapOf(ARTICLE_CONTENT_FIELD to content)
            )
            batch.commit().await()
            UpdateArticleResult.Success
        } catch (e: CancellationException) {
            throw e
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
            val existing = articles.document(articleId).get().await().toArticle()
                ?: return DeleteArticleResult.Error("Artigo não encontrado.")
            if (existing.authorUid != requesterUid) {
                return DeleteArticleResult.Error("Sem permissão para apagar este artigo.")
            }
            firestore.deleteArticleCascade(articleId)
            DeleteArticleResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DeleteArticleResult.Error(e.message ?: "Não foi possível apagar o artigo.")
        }
    }

    override suspend fun recordView(
        articleId: String,
        viewerUid: String
    ): RecordArticleViewResult = try {
        val articleRef = articles.document(articleId)
        val viewRef = articleRef.collection(ARTICLE_VIEWS_SUBCOLLECTION).document(viewerUid)
        val viewCount = firestore.runTransaction { transaction ->
            val articleSnapshot = transaction.get(articleRef)
            if (!articleSnapshot.exists()) return@runTransaction null

            val viewSnapshot = transaction.get(viewRef)
            val currentCount = (articleSnapshot.getLong("viewCount") ?: 0L).coerceAtLeast(0L)
            if (viewSnapshot.exists()) {
                currentCount
            } else {
                transaction.set(
                    viewRef,
                    mapOf(
                        "viewerUid" to viewerUid,
                        "viewedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.update(articleRef, "viewCount", currentCount + 1L)
                currentCount + 1L
            }
        }.await()

        if (viewCount == null) {
            RecordArticleViewResult.Error("Artigo não encontrado.")
        } else {
            RecordArticleViewResult.Success(viewCount.toInt())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        RecordArticleViewResult.Error(e.message ?: "Não foi possível registrar a visualização.")
    }
}