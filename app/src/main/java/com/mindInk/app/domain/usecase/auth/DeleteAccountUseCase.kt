package com.mindInk.app.domain.usecase.auth

import com.mindInk.app.domain.repository.AuthRepository
import com.mindInk.app.domain.repository.DeleteAccountResult
import com.mindInk.app.domain.repository.DeleteUserDataResult
import com.mindInk.app.domain.repository.UserDataRepository
import javax.inject.Inject

private const val RECENT_LOGIN_MESSAGE = "Por segurança, entre novamente com sua conta antes de excluí-la."

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(): DeleteAccountResult {
        val uid = authRepository.currentUid()
            ?: return DeleteAccountResult.Error("Nenhuma conta autenticada.")

        if (!authRepository.hasRecentLogin()) {
            return DeleteAccountResult.Error(RECENT_LOGIN_MESSAGE)
        }

        return when (val cleanup = userDataRepository.deleteAllUserData(uid)) {
            is DeleteUserDataResult.Error -> DeleteAccountResult.Error(cleanup.message)
            DeleteUserDataResult.Success -> when (val result = authRepository.deleteAccount()) {
                DeleteAccountResult.Success -> result
                is DeleteAccountResult.Error -> DeleteAccountResult.Error(
                    "Seus dados foram apagados, mas a conta não foi excluída. ${result.message}"
                )
            }
        }
    }
}