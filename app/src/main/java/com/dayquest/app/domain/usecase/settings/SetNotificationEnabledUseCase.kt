package com.dayquest.app.domain.usecase.settings

import com.dayquest.app.domain.repository.SettingsRepository
import com.dayquest.app.domain.usecase.reminder.ScheduleFixedRemindersUseCase
import javax.inject.Inject

class SetNotificationEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleFixedRemindersUseCase: ScheduleFixedRemindersUseCase
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        if (enabled) {
            scheduleFixedRemindersUseCase.scheduleAll()
        } else {
            scheduleFixedRemindersUseCase.cancelAll()
        }
    }
}
