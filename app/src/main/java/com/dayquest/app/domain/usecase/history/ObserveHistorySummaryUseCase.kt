package com.dayquest.app.domain.usecase.history

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.projection.HistoryDailyProgressRow
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HistoryDailyProgress(
    val dateKey: String,
    val doneCount: Int,
    val totalCount: Int
)

data class HistorySummary(
    val todayDoneCount: Int,
    val todayDeferredCount: Int,
    val todayTotalCount: Int,
    val weeklyDoneCount: Int,
    val weeklyDeferredCount: Int,
    val weeklyTotalCount: Int,
    val dailyProgress: List<HistoryDailyProgress>
)

class ObserveHistorySummaryUseCase @Inject constructor(
    private val dailyItemDao: DailyItemDao
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<HistorySummary> {
        val weekStart = today.with(DayOfWeek.MONDAY)
        val todayKey = today.toString()
        val weekStartKey = weekStart.toString()
        val weekEndKey = today.toString()

        return combine(
            dailyItemDao.observeCountByDate(todayKey),
            dailyItemDao.observeCountByDateAndStatus(todayKey, DailyItemStatus.DONE),
            dailyItemDao.observeCountByDateAndStatus(todayKey, DailyItemStatus.DEFERRED),
            dailyItemDao.observeCountByDateRangeAndStatus(weekStartKey, weekEndKey, DailyItemStatus.DONE),
            dailyItemDao.observeCountByDateRangeAndStatus(weekStartKey, weekEndKey, DailyItemStatus.DEFERRED),
            dailyItemDao.observeCountByDateRange(weekStartKey, weekEndKey),
            dailyItemDao.observeDailyProgressByDateRange(weekStartKey, weekEndKey)
        ) { values ->
            val todayTotal = values[0] as Int
            val todayDone = values[1] as Int
            val todayDeferred = values[2] as Int
            val weeklyDone = values[3] as Int
            val weeklyDeferred = values[4] as Int
            val weeklyTotal = values[5] as Int
            val dailyProgressRows = values[6] as List<HistoryDailyProgressRow>

            HistorySummary(
                todayDoneCount = todayDone,
                todayDeferredCount = todayDeferred,
                todayTotalCount = todayTotal,
                weeklyDoneCount = weeklyDone,
                weeklyDeferredCount = weeklyDeferred,
                weeklyTotalCount = weeklyTotal,
                dailyProgress = dailyProgressRows.map { row ->
                    HistoryDailyProgress(
                        dateKey = row.dateKey,
                        doneCount = row.doneCount,
                        totalCount = row.totalCount
                    )
                }
            )
        }
    }
}
