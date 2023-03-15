package com.kroncoders.android.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val UserPreferences = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Singleton
    @Provides
    fun providePreferenceDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(UserPreferences) }
        )
    }
}

@Singleton
class ChatDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userId: Flow<Long> = dataStore.data.map { preferences ->
        preferences[UserIdKey] ?: -1L
    }

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { preferences -> preferences[UserIdKey] = userId }
    }

    companion object {
        private val UserIdKey = longPreferencesKey("user_id")
    }

}