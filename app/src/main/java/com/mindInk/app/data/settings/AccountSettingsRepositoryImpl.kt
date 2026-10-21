package com.mindInk.app.data.settings

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.mindInk.app.domain.model.AccountSettings
import com.mindInk.app.domain.repository.AccountSettingsRepository
import com.mindInk.app.domain.repository.LoadAccountSettingsResult
import com.mindInk.app.domain.repository.SaveAccountSettingsResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val USERS_COLLECTION = "user_profiles"

internal class AccountSettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AccountSettingsRepository {

    override suspend fun load(uid: String): LoadAccountSettingsResult = try {
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        val settings = snapshot.toObject(AccountSettingsDto::class.java)?.toModel()
            ?: AccountSettings()
        LoadAccountSettingsResult.Success(settings)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LoadAccountSettingsResult.Error("Não foi possível carregar suas informações.")
    }

    override suspend fun save(
        uid: String,
        settings: AccountSettings
    ): SaveAccountSettingsResult = try {
        firestore.collection(USERS_COLLECTION).document(uid).set(
            mapOf(
                "name" to settings.name,
                "github" to settings.github,
                "instagram" to settings.instagram,
                "age" to FieldValue.delete(),
                "linkedin" to FieldValue.delete()
            ),
            SetOptions.merge()
        ).await()
        SaveAccountSettingsResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        SaveAccountSettingsResult.Error("Não foi possível atualizar suas informações.")
    }
}
