package com.dayquest.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class GoalTimeReminderSchedulerTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val scheduler = GoalTimeReminderScheduler(zone)

    @Test
    fun `nextReminder returns nearest future time in same day`() {
        val now = ZonedDateTime.of(2026, 2, 14, 9, 0, 0, 0, zone)

        val next = scheduler.nextReminder(
            now = now,
            goalTimes = listOf(LocalTime.of(7, 0), LocalTime.of(21, 0), LocalTime.of(10, 30))
        )

        assertEquals(ZonedDateTime.of(2026, 2, 14, 10, 30, 0, 0, zone), next)
    }

    @Test
    fun `nextReminder rolls over to next day when all times passed`() {
        val now = ZonedDateTime.of(2026, 2, 14, 23, 0, 0, 0, zone)

        val next = scheduler.nextReminder(
            now = now,
            goalTimes = listOf(LocalTime.of(7, 0), LocalTime.of(21, 0))
        )

        assertEquals(ZonedDateTime.of(2026, 2, 15, 7, 0, 0, 0, zone), next)
    }

    @Test
    fun `nextReminder returns null when no goal times`() {
        val now = ZonedDateTime.of(2026, 2, 14, 9, 0, 0, 0, zone)

        val next = scheduler.nextReminder(now = now, goalTimes = emptyList())

        assertNull(next)
    }

    @Test
    fun `upcomingReminders keeps unique sorted schedule for N days`() {
        val from = ZonedDateTime.of(2026, 2, 14, 20, 0, 0, 0, zone)

        val reminders = scheduler.upcomingReminders(
            from = from,
            goalTimes = listOf(LocalTime.of(21, 0), LocalTime.of(7, 0), LocalTime.of(21, 0)),
            days = 2
        )

        assertEquals(
            listOf(
                ZonedDateTime.of(2026, 2, 14, 21, 0, 0, 0, zone),
                ZonedDateTime.of(2026, 2, 15, 7, 0, 0, 0, zone),
                ZonedDateTime.of(2026, 2, 15, 21, 0, 0, 0, zone)
            ),
            reminders
        )
    }
}
