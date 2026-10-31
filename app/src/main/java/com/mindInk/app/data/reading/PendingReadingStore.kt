package com.mindInk.app.data.reading

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pendingReadingDataStore by preferencesDataStore(
    name = "pending_reading"
)

@Singleton
class PendingReadingStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val key = stringSetPreferencesKey("article_ids")

    val pendingIds: Flow<Set<String>> =
        context.pendingReadingDataStore.data.map { prefs ->
            prefs[key].orEmpty()
        }

    suspend fun add(articleId: String) {
        if (articleId.isBlank()) return
        context.pendingReadingDataStore.edit { prefs ->
            val current = prefs[key].orEmpty().toMutableSet()
            current.add(articleId)
            prefs[key] = current
        }
    }

    suspend fun clear() {
        context.pendingReadingDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    suspend fun getOnce(): Set<String> {
        return context.pendingReadingDataStore.data
            .map { prefs -> prefs[key].orEmpty() }
            .first()
    }
}