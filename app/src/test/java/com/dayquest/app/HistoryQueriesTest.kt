package com.dayquest.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HistoryQueriesTest {

    @Test
    fun `week aggregate sums only current week records`() {
        val ref = LocalDate.of(2026, 2, 14)
        val records = listOf(
            HistoryDailyRecord(ref, "업무", 2),
            HistoryDailyRecord(ref.minusDays(2), "건강", 1),
            HistoryDailyRecord(ref.minusWeeks(1), "업무", 10)
        )

        val total = HistoryQueries.aggregate(records, ref, HistoryPeriod.WEEK)

        assertEquals(3, total)
    }

    @Test
    fun `cumulative by category groups and sums counts`() {
        val ref = LocalDate.of(2026, 2, 14)
        val records = listOf(
            HistoryDailyRecord(ref, "업무", 2),
            HistoryDailyRecord(ref, "업무", 1),
            HistoryDailyRecord(ref, "건강", 3)
        )

        val stats = HistoryQueries.cumulativeByCategory(records, ref, HistoryPeriod.DAY)

        assertEquals(3, stats["업무"])
        assertEquals(3, stats["건강"])
    }
}
