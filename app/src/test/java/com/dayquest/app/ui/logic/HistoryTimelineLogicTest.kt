package com.dayquest.app.ui.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryTimelineLogicTest {

    private val dateFormatter = DateTimeFormatter.ofPattern("M/d")
    private val weekdayFormatter = DateTimeFormatter.ofPattern("E", Locale.KOREAN)

    @Test
    fun buildHistoryTimeline_fills_missing_days_with_zeroes() {
        val result = buildHistoryTimeline(
            today = LocalDate.of(2026, 2, 18),
            periodDays = 7,
            progress = listOf(
                HistoryProgressSeed(dateKey = "2026-02-18", doneCount = 2, deferredCount = 1, totalCount = 4),
                HistoryProgressSeed(dateKey = "2026-02-16", doneCount = 1, deferredCount = 0, totalCount = 3)
            ),
            dateFormatter = dateFormatter,
            weekdayFormatter = weekdayFormatter
        )

        assertEquals(7, result.size)
        assertEquals("2026-02-18", result[0].dateKey)
        assertEquals("2026-02-17", result[1].dateKey)
        assertEquals(0, result[1].totalCount)
        assertEquals("2026-02-16", result[2].dateKey)
        assertEquals(1, result[2].doneCount)
        assertEquals("2026-02-12", result.last().dateKey)
    }

    @Test
    fun buildHistoryTimeline_computes_completion_rate_per_day() {
        val result = buildHistoryTimeline(
            today = LocalDate.of(2026, 2, 18),
            periodDays = 1,
            progress = listOf(
                HistoryProgressSeed(dateKey = "2026-02-18", doneCount = 3, deferredCount = 1, totalCount = 5)
            ),
            dateFormatter = dateFormatter,
            weekdayFormatter = weekdayFormatter
        )

        assertEquals(1, result.size)
        assertEquals(60, result.single().completionRate)
    }

    @Test
    fun filterHistoryTimeline_when_active_only_removes_zero_total_days() {
        val timeline = buildHistoryTimeline(
            today = LocalDate.of(2026, 2, 18),
            periodDays = 3,
            progress = listOf(
                HistoryProgressSeed(dateKey = "2026-02-18", doneCount = 1, deferredCount = 0, totalCount = 1)
            ),
            dateFormatter = dateFormatter,
            weekdayFormatter = weekdayFormatter
        )

        val filtered = filterHistoryTimeline(timeline, showOnlyActiveDays = true)

        assertEquals(1, filtered.size)
        assertEquals("2026-02-18", filtered.single().dateKey)
    }
}
