package com.mindInk.app.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mindInk.app.R
import com.mindInk.app.domain.repository.AuthErrorReason
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleCredentialProvider"

internal sealed interface GoogleTokenResult {
    data class Token(val idToken: String) : GoogleTokenResult
    data object Cancelled : GoogleTokenResult
    data class Error(val reason: AuthErrorReason) : GoogleTokenResult
}

@Singleton
internal class GoogleCredentialProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    suspend fun getIdToken(activity: Activity): GoogleTokenResult {
        return when (val authorized = requestGoogleIdToken(activity, filterAuthorized = true)) {
            is GoogleTokenResult.Error -> {
                if (authorized.reason == AuthErrorReason.NO_GOOGLE_ACCOUNT) {
                    requestGoogleIdToken(activity, filterAuthorized = false)
                } else {
                    authorized
                }
            }
            else -> authorized
        }
    }

    private suspend fun requestGoogleIdToken(
        activity: Activity,
        filterAuthorized: Boolean
    ): GoogleTokenResult {
        val webClientId = appContext.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterAuthorized)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val credential = CredentialManager.create(activity)
                .getCredential(activity, request)
                .credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleTokenResult.Token(googleCredential.idToken)
            } else {
                GoogleTokenResult.Error(AuthErrorReason.INVALID_CREDENTIAL)
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleTokenResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleTokenResult.Error(AuthErrorReason.NO_GOOGLE_ACCOUNT)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Falha ao obter credencial do Google (filterAuthorized=$filterAuthorized)", e)
            GoogleTokenResult.Error(AuthErrorReason.UNKNOWN)
        } catch (e: GoogleIdTokenParsingException) {
            Log.w(TAG, "Token do Google inválido", e)
            GoogleTokenResult.Error(AuthErrorReason.INVALID_CREDENTIAL)
        }
    }

    suspend fun clearState() {
        try {
            CredentialManager.create(appContext)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Log.w(TAG, "Falha ao limpar estado de credenciais", e)
        }
    }
}