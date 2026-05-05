package com.dayquest.app.domain.usecase.reminder

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleFixedRemindersUseCaseTest {

    @Test
    fun schedules_07_and_21_fixed_reminders() = runBlocking {
        val scheduler = FakeReminderScheduler()
        val useCase = ScheduleFixedRemindersUseCase(scheduler)

        useCase()

        assertEquals(
            listOf(
                Triple(ScheduleFixedRemindersUseCase.MORNING_REMINDER_ID, 7, 0),
                Triple(ScheduleFixedRemindersUseCase.EVENING_REMINDER_ID, 21, 0)
            ),
            scheduler.calls
        )
    }
}

private class FakeReminderScheduler : ReminderScheduler {
    val calls = mutableListOf<Triple<String, Int, Int>>()

    override suspend fun scheduleDaily(id: String, hour: Int, minute: Int) {
        calls += Triple(id, hour, minute)
    }
}
