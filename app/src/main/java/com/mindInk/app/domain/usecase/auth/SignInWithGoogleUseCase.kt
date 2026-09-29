package com.mindInk.app.domain.usecase.auth

import android.app.Activity
import com.mindInk.app.domain.repository.AuthRepository
import com.mindInk.app.domain.repository.SignInResult
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(activity: Activity): SignInResult =
        repository.signInWithGoogle(activity)
}
