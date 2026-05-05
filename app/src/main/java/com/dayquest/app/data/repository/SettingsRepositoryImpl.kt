package com.dayquest.app.data.repository

import android.content.Context
import com.dayquest.app.data.local.db.DayQuestDatabase
import com.dayquest.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val database: DayQuestDatabase
) : SettingsRepository {

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val notificationState = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )

    override fun observeNotificationsEnabled(): Flow<Boolean> = notificationState.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        notificationState.value = enabled
    }

    override suspend fun resetLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    private companion object {
        const val PREF_NAME = "dayquest_settings"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
