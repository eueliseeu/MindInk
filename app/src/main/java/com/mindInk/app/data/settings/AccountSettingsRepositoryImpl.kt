package com.mindInk.app.data.settings

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mindInk.app.data.profile.USER_FIELD_GITHUB
import com.mindInk.app.data.profile.USER_FIELD_LINKEDIN
import com.mindInk.app.data.profile.USER_FIELD_NAME
import com.mindInk.app.data.profile.USER_FIELD_PHOTO_URL
import com.mindInk.app.data.profile.USER_FIELD_UPDATED_AT
import com.mindInk.app.data.profile.users
import com.mindInk.app.domain.model.AccountSettings
import com.mindInk.app.domain.repository.AccountSettingsRepository
import com.mindInk.app.domain.repository.LoadAccountSettingsResult
import com.mindInk.app.domain.repository.SaveAccountSettingsResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class AccountSettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AccountSettingsRepository {

    override suspend fun load(uid: String): LoadAccountSettingsResult = try {
        val snapshot = firestore.users().document(uid).get().await()
        val settings = if (snapshot.exists()) {
            AccountSettings(
                name = snapshot.getString(USER_FIELD_NAME).orEmpty(),
                github = snapshot.getString(USER_FIELD_GITHUB).orEmpty(),
                linkedin = snapshot.getString(USER_FIELD_LINKEDIN).orEmpty(),
                photoUrl = snapshot.getString(USER_FIELD_PHOTO_URL)
            )
        } else {
            AccountSettings()
        }
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
        firestore.users().document(uid).set(
            mapOf(
                USER_FIELD_NAME to settings.name,
                USER_FIELD_GITHUB to settings.github,
                USER_FIELD_LINKEDIN to settings.linkedin,
                USER_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
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