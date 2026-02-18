package com.dayquest.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeNotificationsEnabled(): Flow<Boolean>
    suspend fun getNotificationsEnabled(): Boolean
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun resetLocalData()
}
