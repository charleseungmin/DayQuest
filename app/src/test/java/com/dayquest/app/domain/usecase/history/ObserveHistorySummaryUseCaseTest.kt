package com.dayquest.app.domain.usecase.history

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.data.local.projection.HistoryDailyProgressRow
import com.dayquest.app.data.local.projection.TodayTaskRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveHistorySummaryUseCaseTest {

    @Test
    fun includes_deferred_count_in_recent_daily_progress() = runBlocking {
        val useCase = ObserveHistorySummaryUseCase(
            dailyItemDao = FakeDailyItemDao(
                todayTotal = 4,
                todayDone = 2,
                todayDeferred = 1,
                rows = listOf(
                    HistoryDailyProgressRow(
                        dateKey = "2026-02-17",
                        totalCount = 4,
                        doneCount = 2,
                        deferredCount = 1
                    )
                )
            )
        )

        val summary = useCase(today = java.time.LocalDate.parse("2026-02-17")).first()

        assertEquals(1, summary.recentDailyProgress.size)
        assertEquals(1, summary.recentDailyProgress.first().deferredCount)
        assertEquals(2, summary.todayDoneCount)
    }
}

private class FakeDailyItemDao(
    private val todayTotal: Int,
    private val todayDone: Int,
    private val todayDeferred: Int,
    private val rows: List<HistoryDailyProgressRow>
) : DailyItemDao {
    override fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>> = flowOf(emptyList())

    override fun observeTodayTasks(dateKey: String): Flow<List<TodayTaskRow>> = flowOf(emptyList())

    override suspend fun insert(item: DailyItemEntity): Long = 0L

    override suspend fun insertAll(items: List<DailyItemEntity>): List<Long> = emptyList()

    override suspend fun update(item: DailyItemEntity) = Unit

    override suspend fun getById(id: Long): DailyItemEntity? = null

    override suspend fun updateState(
        id: Long,
        status: DailyItemStatus,
        completedAt: Long?,
        deferredToDateKey: String?
    ) = Unit

    override suspend fun countByDate(dateKey: String): Int = todayTotal

    override fun observeCountByDate(dateKey: String): Flow<Int> = flowOf(todayTotal)

    override suspend fun countByDateAndStatus(dateKey: String, status: DailyItemStatus): Int =
        when (status) {
            DailyItemStatus.DONE -> todayDone
            DailyItemStatus.DEFERRED -> todayDeferred
            else -> 0
        }

    override fun observeCountByDateAndStatus(dateKey: String, status: DailyItemStatus): Flow<Int> =
        flowOf(
            when (status) {
                DailyItemStatus.DONE -> todayDone
                DailyItemStatus.DEFERRED -> todayDeferred
                else -> 0
            }
        )

    override fun observeCountByDateRangeAndStatus(
        startDateKey: String,
        endDateKey: String,
        status: DailyItemStatus
    ): Flow<Int> = flowOf(0)

    override fun observeCountByDateRange(startDateKey: String, endDateKey: String): Flow<Int> =
        flowOf(0)

    override fun observeDailyProgressByDateRange(
        startDateKey: String,
        endDateKey: String
    ): Flow<List<HistoryDailyProgressRow>> = flowOf(rows)

    override suspend fun countImportantByDateAndStatus(dateKey: String, status: DailyItemStatus): Int = 0
}
