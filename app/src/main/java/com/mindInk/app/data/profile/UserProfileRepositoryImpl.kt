package com.mindInk.app.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.mindInk.app.domain.repository.PublicUserProfile
import com.mindInk.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION = "user_profiles"

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserProfileRepository {

    override suspend fun getProfiles(
        uids: Collection<String>
    ): Map<String, PublicUserProfile> {
        if (uids.isEmpty()) return emptyMap()
        val result = linkedMapOf<String, PublicUserProfile>()
        try {
            uids.distinct().chunked(10).forEach { chunk ->
                chunk.forEach { uid ->
                    val snap = firestore.collection(COLLECTION).document(uid).get().await()
                    if (!snap.exists()) return@forEach
                    val name = snap.getString("name")
                        ?.takeIf { it.isNotBlank() }
                        ?: return@forEach
                    val photoUrl = snap.getString("photoUrl")
                        ?: snap.getString("photo_url")
                    result[uid] = PublicUserProfile(
                        uid = uid,
                        name = name,
                        photoUrl = photoUrl
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
        return result
    }
}