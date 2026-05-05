package com.dayquest.app.domain.usecase.settings

import com.dayquest.app.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveNotificationEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.observeNotificationsEnabled()
}
