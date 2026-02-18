package com.dayquest.app.domain.usecase.reminder

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleFixedRemindersUseCaseTest {

    @Test
    fun schedules_07_and_21_fixed_reminders() = runBlocking {
        val scheduler = FakeReminderScheduler()
        val useCase = ScheduleFixedRemindersUseCase(scheduler)

        useCase.scheduleAll()

        assertEquals(
            listOf(
                Triple(ScheduleFixedRemindersUseCase.MORNING_REMINDER_ID, 7, 0),
                Triple(ScheduleFixedRemindersUseCase.EVENING_REMINDER_ID, 21, 0)
            ),
            scheduler.scheduleCalls
        )
    }

    @Test
    fun cancels_both_fixed_reminders() = runBlocking {
        val scheduler = FakeReminderScheduler()
        val useCase = ScheduleFixedRemindersUseCase(scheduler)

        useCase.cancelAll()

        assertEquals(
            listOf(
                ScheduleFixedRemindersUseCase.MORNING_REMINDER_ID,
                ScheduleFixedRemindersUseCase.EVENING_REMINDER_ID
            ),
            scheduler.cancelCalls
        )
    }
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
