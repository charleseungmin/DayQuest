package com.dayquest.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DomainRuleTest {

    @Test
    fun `daily rule always schedules`() {
        val rule = RecurrenceRule(type = RecurrenceType.DAILY)

        assertTrue(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 14), rule))
        assertTrue(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 15), rule))
    }

    @Test
    fun `weekly rule schedules only matching weekdays`() {
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        assertTrue(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 16), rule)) // Monday
        assertFalse(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 17), rule)) // Tuesday
    }

    @Test
    fun `monthly rule schedules only matching day of month`() {
        val rule = RecurrenceRule(
            type = RecurrenceType.MONTHLY,
            daysOfMonth = setOf(1, 15, 28)
        )

        assertTrue(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 15), rule))
        assertFalse(RecurrenceEvaluator.shouldScheduleOn(LocalDate.of(2026, 2, 14), rule))
    }

    @Test
    fun `streak resets when current date not completed`() {
        val completed = setOf(
            LocalDate.of(2026, 2, 13),
            LocalDate.of(2026, 2, 12)
        )

        val streak = StreakCalculator.calculate(LocalDate.of(2026, 2, 14), completed)

        assertEquals(0, streak)
    }

    @Test
    fun `streak counts consecutive completion across midnight boundary`() {
        val completed = setOf(
            LocalDate.of(2026, 2, 14),
            LocalDate.of(2026, 2, 13),
            LocalDate.of(2026, 2, 12),
            LocalDate.of(2026, 2, 10)
        )

        val streak = StreakCalculator.calculate(LocalDate.of(2026, 2, 14), completed)

        assertEquals(3, streak)
    }
}
