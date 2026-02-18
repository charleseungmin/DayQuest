package com.dayquest.app.domain.usecase.reminder

import javax.inject.Inject

interface ReminderScheduler {
    suspend fun scheduleDaily(id: String, hour: Int, minute: Int)
    suspend fun cancelDaily(id: String)
}

class ScheduleFixedRemindersUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke() {
        scheduleAll()
    }

    suspend fun scheduleAll() {
        reminderScheduler.scheduleDaily(id = MORNING_REMINDER_ID, hour = 7, minute = 0)
        reminderScheduler.scheduleDaily(id = EVENING_REMINDER_ID, hour = 21, minute = 0)
    }

    suspend fun cancelAll() {
        reminderScheduler.cancelDaily(MORNING_REMINDER_ID)
        reminderScheduler.cancelDaily(EVENING_REMINDER_ID)
    }

    companion object {
        const val MORNING_REMINDER_ID = "fixed_morning_0700"
        const val EVENING_REMINDER_ID = "fixed_evening_2100"
    }
}
