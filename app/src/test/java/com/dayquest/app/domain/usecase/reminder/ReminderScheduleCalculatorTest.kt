package com.dayquest.app.domain.usecase.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ReminderScheduleCalculatorTest {

    @Test
    fun returns_delay_until_today_when_target_is_future() {
        val now = LocalDateTime.of(2026, 2, 14, 6, 30, 0)

        val delay = ReminderScheduleCalculator.initialDelayMillis(now, targetHour = 7, targetMinute = 0)

        assertEquals(30 * 60 * 1000L, delay)
    }

    @Test
    fun returns_delay_until_next_day_when_target_passed() {
        val now = LocalDateTime.of(2026, 2, 14, 22, 0, 0)

        val delay = ReminderScheduleCalculator.initialDelayMillis(now, targetHour = 21, targetMinute = 0)

        assertEquals(23 * 60 * 60 * 1000L, delay)
    }
}
