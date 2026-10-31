package com.mindInk.app.domain.repository

data class PublicUserProfile(
    val uid: String,
    val name: String,
    val photoUrl: String? = null
)

interface UserProfileRepository {
    suspend fun getProfiles(uids: Collection<String>): Map<String, PublicUserProfile>
}