package com.dayquest.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class HistoryPeriod { DAY, WEEK, MONTH }

data class HistoryDailyRecord(
    val date: LocalDate,
    val category: String,
    val completedCount: Int
)

object HistoryQueries {
    fun aggregate(records: List<HistoryDailyRecord>, referenceDate: LocalDate, period: HistoryPeriod): Int {
        return records.asSequence()
            .filter { isInPeriod(it.date, referenceDate, period) }
            .sumOf { it.completedCount }
    }

    fun cumulativeByCategory(
        records: List<HistoryDailyRecord>,
        referenceDate: LocalDate,
        period: HistoryPeriod
    ): Map<String, Int> {
        return records.asSequence()
            .filter { isInPeriod(it.date, referenceDate, period) }
            .groupBy { it.category }
            .mapValues { (_, values) -> values.sumOf { it.completedCount } }
            .toSortedMap()
    }

    // 날짜 단위 조회 성능 개선용 인덱스(선형 검색 회피)
    fun buildDateIndex(records: List<HistoryDailyRecord>): Map<LocalDate, List<HistoryDailyRecord>> {
        return records.groupBy { it.date }
    }

    private fun isInPeriod(date: LocalDate, ref: LocalDate, period: HistoryPeriod): Boolean {
        return when (period) {
            HistoryPeriod.DAY -> date == ref
            HistoryPeriod.WEEK -> {
                val start = ref.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                !date.isBefore(start) && !date.isAfter(end)
            }

            HistoryPeriod.MONTH -> date.year == ref.year && date.month == ref.month
        }
    }
}

@Composable
fun HistoryScreen() {
    val today = LocalDate.now()
    var period by remember { mutableStateOf(HistoryPeriod.DAY) }
    val records = remember {
        listOf(
            HistoryDailyRecord(today, "건강", 2),
            HistoryDailyRecord(today.minusDays(1), "업무", 3),
            HistoryDailyRecord(today.minusDays(2), "학습", 1),
            HistoryDailyRecord(today.minusWeeks(1), "업무", 4),
            HistoryDailyRecord(today.withDayOfMonth(1), "건강", 5)
        )
    }

    val total = HistoryQueries.aggregate(records, today, period)
    val byCategory = HistoryQueries.cumulativeByCategory(records, today, period)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("HistoryScreen", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { period = HistoryPeriod.DAY }) { Text("일") }
                Button(onClick = { period = HistoryPeriod.WEEK }) { Text("주") }
                Button(onClick = { period = HistoryPeriod.MONTH }) { Text("월") }
            }

            Text("선택 기간: $period")
            Text("완료 누적: $total")

            if (byCategory.isEmpty()) {
                Text("해당 기간 기록이 없습니다.")
            } else {
                Text("카테고리별 통계", style = MaterialTheme.typography.titleMedium)
                byCategory.forEach { (category, count) ->
                    Text("- $category: $count")
                }
            }
        }
    }
}
