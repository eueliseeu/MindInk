package com.mindInk.app.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.mindInk.app.data.article.ARTICLES_COLLECTION
import com.mindInk.app.data.article.deleteArticleCascade
import com.mindInk.app.data.common.deleteInBatches
import com.mindInk.app.data.favorite.favoriteAuthors
import com.mindInk.app.data.profile.users
import com.mindInk.app.data.reading.readingProgress
import com.mindInk.app.data.star.stars
import com.mindInk.app.domain.repository.DeleteUserDataResult
import com.mindInk.app.domain.repository.UserDataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 50L

@Singleton
class UserDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataRepository {

    private val articles = firestore.collection(ARTICLES_COLLECTION)

    override suspend fun deleteAllUserData(uid: String): DeleteUserDataResult = try {
        removeGivenStars(uid)
        deleteOwnArticles(uid)
        firestore.deleteInBatches(firestore.favoriteAuthors(uid))
        firestore.deleteInBatches(firestore.readingProgress(uid))
        firestore.users().document(uid).delete().await()
        DeleteUserDataResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DeleteUserDataResult.Error(
            "Não foi possível apagar seus dados. Verifique a conexão e tente novamente."
        )
    }

    private suspend fun removeGivenStars(uid: String) {
        while (true) {
            val page = firestore.stars(uid).limit(PAGE_SIZE).get(Source.SERVER).await()
            if (page.isEmpty) return
            page.documents.forEach { star -> removeStar(uid, star.id) }
        }
    }

    private suspend fun removeStar(uid: String, articleId: String) {
        firestore.runTransaction { transaction ->
            val articleRef = articles.document(articleId)
            val starRef = firestore.stars(uid).document(articleId)

            val article = transaction.get(articleRef)
            val star = transaction.get(starRef)
            if (!star.exists()) return@runTransaction Unit

            transaction.delete(starRef)
            if (article.exists()) {
                val count = (article.getLong("starCount") ?: 0L).toInt()
                transaction.update(articleRef, "starCount", (count - 1).coerceAtLeast(0))
            }
            Unit
        }.await()
    }

    private suspend fun deleteOwnArticles(uid: String) {
        while (true) {
            val page = articles
                .whereEqualTo("authorUid", uid)
                .limit(PAGE_SIZE)
                .get(Source.SERVER)
                .await()
            if (page.isEmpty) return
            page.documents.forEach { article -> firestore.deleteArticleCascade(article.id) }
        }
    }
}