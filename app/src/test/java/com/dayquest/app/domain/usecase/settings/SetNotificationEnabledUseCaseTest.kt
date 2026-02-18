package com.dayquest.app.domain.usecase.settings

import com.dayquest.app.domain.repository.SettingsRepository
import com.dayquest.app.domain.usecase.reminder.ReminderScheduler
import com.dayquest.app.domain.usecase.reminder.ScheduleFixedRemindersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SetNotificationEnabledUseCaseTest {

    @Test
    fun enabling_notifications_schedules_fixed_reminders() = runBlocking {
        val repository = FakeSettingsRepository(initialEnabled = false)
        val reminderScheduler = FakeReminderScheduler()
        val useCase = SetNotificationEnabledUseCase(
            settingsRepository = repository,
            scheduleFixedRemindersUseCase = ScheduleFixedRemindersUseCase(reminderScheduler)
        )

        useCase(true)

        assertEquals(true, repository.getNotificationsEnabled())
        assertEquals(
            listOf(
                Triple(ScheduleFixedRemindersUseCase.MORNING_REMINDER_ID, 7, 0),
                Triple(ScheduleFixedRemindersUseCase.EVENING_REMINDER_ID, 21, 0)
            ),
            reminderScheduler.scheduleCalls
        )
    }

    @Test
    fun disabling_notifications_cancels_fixed_reminders() = runBlocking {
        val repository = FakeSettingsRepository(initialEnabled = true)
        val reminderScheduler = FakeReminderScheduler()
        val useCase = SetNotificationEnabledUseCase(
            settingsRepository = repository,
            scheduleFixedRemindersUseCase = ScheduleFixedRemindersUseCase(reminderScheduler)
        )

        useCase(false)

        assertEquals(false, repository.getNotificationsEnabled())
        assertEquals(
            listOf(
                ScheduleFixedRemindersUseCase.MORNING_REMINDER_ID,
                ScheduleFixedRemindersUseCase.EVENING_REMINDER_ID
            ),
            reminderScheduler.cancelCalls
        )
    }
}

private class FakeSettingsRepository(initialEnabled: Boolean) : SettingsRepository {
    private val notificationFlow = MutableStateFlow(initialEnabled)

    override fun observeNotificationsEnabled(): Flow<Boolean> = notificationFlow

    override suspend fun getNotificationsEnabled(): Boolean = notificationFlow.value

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        notificationFlow.value = enabled
    }

    override suspend fun resetLocalData() = Unit
}

private class FakeReminderScheduler : ReminderScheduler {
    val scheduleCalls = mutableListOf<Triple<String, Int, Int>>()
    val cancelCalls = mutableListOf<String>()

    override suspend fun scheduleDaily(id: String, hour: Int, minute: Int) {
        scheduleCalls += Triple(id, hour, minute)
    }

    override suspend fun cancelDaily(id: String) {
        cancelCalls += id
    }
}
