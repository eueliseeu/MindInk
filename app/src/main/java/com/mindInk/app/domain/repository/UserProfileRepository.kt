package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.AuthUser

data class PublicUserProfile(
    val uid: String,
    val name: String,
    val photoUrl: String? = null
)

interface UserProfileRepository {
    suspend fun getProfiles(uids: Collection<String>): Map<String, PublicUserProfile>
    suspend fun ensureProfile(user: AuthUser): Boolean
}