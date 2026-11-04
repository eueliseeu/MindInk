package com.mindInk.app.domain.repository

sealed interface DeleteUserDataResult {
    data object Success : DeleteUserDataResult
    data class Error(val message: String) : DeleteUserDataResult
}

interface UserDataRepository {
    suspend fun deleteAllUserData(uid: String): DeleteUserDataResult
}