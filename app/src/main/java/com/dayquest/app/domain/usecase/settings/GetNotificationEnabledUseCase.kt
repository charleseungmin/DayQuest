package com.dayquest.app.domain.usecase.settings

import com.dayquest.app.domain.repository.SettingsRepository
import javax.inject.Inject

class GetNotificationEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = settingsRepository.getNotificationsEnabled()
}
