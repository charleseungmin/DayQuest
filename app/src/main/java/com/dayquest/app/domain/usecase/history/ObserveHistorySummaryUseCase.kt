package com.dayquest.app.domain.usecase.history

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.projection.HistoryDailyProgressRow
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HistoryDailyProgress(
    val dateKey: String,
    val doneCount: Int,
    val deferredCount: Int,
    val totalCount: Int
)

data class HistorySummary(
    val todayDoneCount: Int,
    val todayDeferredCount: Int,
    val todayTotalCount: Int,
    val recentDailyProgress: List<HistoryDailyProgress>
)

class ObserveHistorySummaryUseCase @Inject constructor(
    private val dailyItemDao: DailyItemDao
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<HistorySummary> {
        val todayKey = today.toString()
        val recentStartKey = today.minusDays(29).toString()

        return combine(
            dailyItemDao.observeCountByDate(todayKey),
            dailyItemDao.observeCountByDateAndStatus(todayKey, DailyItemStatus.DONE),
            dailyItemDao.observeCountByDateAndStatus(todayKey, DailyItemStatus.DEFERRED),
            dailyItemDao.observeDailyProgressByDateRange(recentStartKey, todayKey)
        ) { values ->
            val todayTotal = values[0] as Int
            val todayDone = values[1] as Int
            val todayDeferred = values[2] as Int
            val dailyProgressRows = values[3] as List<HistoryDailyProgressRow>

            HistorySummary(
                todayDoneCount = todayDone,
                todayDeferredCount = todayDeferred,
                todayTotalCount = todayTotal,
                recentDailyProgress = dailyProgressRows.map { row ->
                    HistoryDailyProgress(
                        dateKey = row.dateKey,
                        doneCount = row.doneCount,
                        deferredCount = row.deferredCount,
                        totalCount = row.totalCount
                    )
                }
            )
        }
    }
}
