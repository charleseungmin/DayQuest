package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class UpdateDailyItemStatusUseCaseTest {

    @Test
    fun done_transition_sets_completed_time() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)

        useCase(1L, DailyItemStatus.DONE, nowEpochMillis = 1234L)

        val updated = dao.getById(1L)
        assertEquals(DailyItemStatus.DONE, updated?.status)
        assertEquals(1234L, updated?.completedAtEpochMillis)
    }

    @Test
    fun deferred_transition_sets_defer_date() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)

        useCase(1L, DailyItemStatus.DEFERRED, nowEpochMillis = 2000L, deferToDate = LocalDate.of(2026, 2, 15))

        val updated = dao.getById(1L)
        assertEquals(DailyItemStatus.DEFERRED, updated?.status)
        assertEquals("2026-02-15", updated?.deferredToDateKey)
        assertEquals(null, updated?.completedAtEpochMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun deferred_without_date_throws() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)
        useCase(1L, DailyItemStatus.DEFERRED, nowEpochMillis = 2000L)
    }
}

private class FakeDailyItemDaoForUpdate : DailyItemDao {
    private val items = mutableMapOf<Long, DailyItemEntity>(
        1L to DailyItemEntity(id = 1L, dateKey = "2026-02-14", taskId = 10L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L)
    )

    override fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>> = flowOf(items.values.filter { it.dateKey == dateKey })
    override suspend fun insert(item: DailyItemEntity): Long { items[item.id] = item; return item.id }
    override suspend fun insertAll(items: List<DailyItemEntity>): List<Long> = items.map { insert(it) }
    override suspend fun update(item: DailyItemEntity) { items[item.id] = item }
    override suspend fun getById(id: Long): DailyItemEntity? = items[id]

    override suspend fun updateState(id: Long, status: DailyItemStatus, completedAt: Long?, deferredToDateKey: String?) {
        val current = items[id] ?: return
        items[id] = current.copy(
            status = status,
            completedAtEpochMillis = completedAt,
            deferredToDateKey = deferredToDateKey
        )
    }

    override suspend fun countByDate(dateKey: String): Int = items.values.count { it.dateKey == dateKey }
}
