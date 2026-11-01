package com.mindInk.app.data.profile

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.PublicUserProfile
import com.mindInk.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val WHERE_IN_LIMIT = 10

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserProfileRepository {

    override suspend fun getProfiles(
        uids: Collection<String>
    ): Map<String, PublicUserProfile> {
        if (uids.isEmpty()) return emptyMap()
        return try {
            uids.distinct()
                .chunked(WHERE_IN_LIMIT)
                .flatMap { chunk ->
                    firestore.users()
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()
                        .documents
                }
                .associate { doc ->
                    doc.id to PublicUserProfile(
                        uid = doc.id,
                        name = doc.getString(USER_FIELD_NAME).orEmpty(),
                        photoUrl = doc.getString(USER_FIELD_PHOTO_URL)
                    )
                }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override suspend fun ensureProfile(user: AuthUser): Boolean = try {
        val ref = firestore.users().document(user.uid)
        val snapshot = ref.get().await()
        val photoUrl = user.photoUrl

        when {
            !snapshot.exists() -> ref.set(
                mapOf(
                    USER_FIELD_NAME to user.displayName.orEmpty(),
                    USER_FIELD_PHOTO_URL to photoUrl,
                    USER_FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                    USER_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
            ).await()

            photoUrl != null && snapshot.getString(USER_FIELD_PHOTO_URL) != photoUrl -> ref.update(
                mapOf(
                    USER_FIELD_PHOTO_URL to photoUrl,
                    USER_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
            ).await()
        }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}