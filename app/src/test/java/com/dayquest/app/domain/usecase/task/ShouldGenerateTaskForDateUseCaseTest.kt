package com.dayquest.app.domain.usecase.task

import com.dayquest.app.core.model.RepeatType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ShouldGenerateTaskForDateUseCaseTest {

    private val useCase = ShouldGenerateTaskForDateUseCase()

    @Test
    fun daily_repeat_should_generate_for_any_future_date() {
        val base = LocalDate.of(2026, 2, 1)
        val target = LocalDate.of(2026, 2, 13)

        assertTrue(useCase(RepeatType.DAILY, base, target, null))
    }

    @Test
    fun weekly_repeat_should_match_only_selected_days() {
        val base = LocalDate.of(2026, 2, 1)
        val mask = ShouldGenerateTaskForDateUseCase.makeWeeklyMask(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

        assertTrue(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 13), mask)) // Fri
        assertFalse(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 12), mask)) // Thu
    }

    @Test
    fun monthly_repeat_should_fallback_to_last_day_when_needed() {
        val base = LocalDate.of(2026, 1, 31)

        assertTrue(useCase(RepeatType.MONTHLY, base, LocalDate.of(2026, 2, 28), null))
        assertFalse(useCase(RepeatType.MONTHLY, base, LocalDate.of(2026, 2, 27), null))
    }

    @Test
    fun should_not_generate_for_date_before_base() {
        val base = LocalDate.of(2026, 2, 10)
        val target = LocalDate.of(2026, 2, 9)

        assertFalse(useCase(RepeatType.DAILY, base, target, null))
    }

    @Test
    fun weekly_repeat_without_mask_should_fallback_to_base_day() {
        val base = LocalDate.of(2026, 2, 10) // Tuesday

        assertTrue(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 17), null)) // Tuesday
        assertFalse(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 18), null)) // Wednesday
    }

    @Test
    fun custom_repeat_with_zero_mask_should_fallback_to_base_day() {
        val base = LocalDate.of(2026, 2, 12) // Thursday

        assertTrue(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 19), 0)) // Thursday
        assertFalse(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 20), 0)) // Friday
    }

    @Test
    fun weekly_mask_should_support_sunday_bit_correctly() {
        val base = LocalDate.of(2026, 2, 10) // Tuesday
        val sundayOnly = ShouldGenerateTaskForDateUseCase.makeWeeklyMask(DayOfWeek.SUNDAY)

        assertTrue(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 15), sundayOnly)) // Sunday
        assertFalse(useCase(RepeatType.WEEKLY, base, LocalDate.of(2026, 2, 16), sundayOnly)) // Monday
    }

    @Test
    fun custom_repeat_should_use_provided_mask_instead_of_base_day() {
        val base = LocalDate.of(2026, 2, 10) // Tuesday
        val mondayAndWednesday = ShouldGenerateTaskForDateUseCase.makeWeeklyMask(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        assertTrue(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 11), mondayAndWednesday)) // Wednesday
        assertFalse(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 17), mondayAndWednesday)) // Tuesday
    }

    @Test
    fun monthly_repeat_should_handle_leap_year_end_of_month() {
        val base = LocalDate.of(2024, 1, 31)

        assertTrue(useCase(RepeatType.MONTHLY, base, LocalDate.of(2024, 2, 29), null))
        assertFalse(useCase(RepeatType.MONTHLY, base, LocalDate.of(2024, 2, 28), null))
    }

    @Test
    fun custom_repeat_with_null_mask_should_fallback_to_base_day() {
        val base = LocalDate.of(2026, 2, 11) // Wednesday

        assertTrue(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 18), null)) // Wednesday
        assertFalse(useCase(RepeatType.CUSTOM, base, LocalDate.of(2026, 2, 19), null)) // Thursday
    }
}
