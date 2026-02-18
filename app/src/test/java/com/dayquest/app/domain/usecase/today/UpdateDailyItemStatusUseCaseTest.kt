package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
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

    @Test
    fun deferred_transition_creates_next_date_item_once_per_task() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)

        useCase(1L, DailyItemStatus.DEFERRED, nowEpochMillis = 2000L, deferToDate = LocalDate.of(2026, 2, 15))

        val deferredItem = dao.findByDateAndTask("2026-02-15", 10L)
        assertNotNull(deferredItem)
        assertEquals(DailyItemStatus.TODO, deferredItem?.status)
    }

    @Test
    fun deferred_transition_does_not_create_duplicate_for_same_task() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate(
            initialItems = listOf(
                DailyItemEntity(id = 1L, dateKey = "2026-02-14", taskId = 10L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L),
                DailyItemEntity(id = 2L, dateKey = "2026-02-15", taskId = 10L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L)
            )
        )
        val useCase = UpdateDailyItemStatusUseCase(dao)

        useCase(1L, DailyItemStatus.DEFERRED, nowEpochMillis = 2000L, deferToDate = LocalDate.of(2026, 2, 15))

        assertEquals(1, dao.countByDateAndTask("2026-02-15", 10L))
    }

    @Test
    fun deferred_without_date_defaults_to_next_day_and_creates_item() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)

        useCase(1L, DailyItemStatus.DEFERRED, nowEpochMillis = 2000L)

        val updated = dao.getById(1L)
        assertEquals(DailyItemStatus.DEFERRED, updated?.status)
        assertEquals("2026-02-15", updated?.deferredToDateKey)

        val deferredItem = dao.findByDateAndTask("2026-02-15", 10L)
        assertNotNull(deferredItem)
        assertEquals(DailyItemStatus.TODO, deferredItem?.status)
        assertEquals(2000L, deferredItem?.createdAtEpochMillis)
    }

    @Test
    fun deferred_with_past_date_throws_exception() = runBlocking {
        val dao = FakeDailyItemDaoForUpdate()
        val useCase = UpdateDailyItemStatusUseCase(dao)

        try {
            useCase(
                1L,
                DailyItemStatus.DEFERRED,
                nowEpochMillis = 2000L,
                deferToDate = LocalDate.of(2026, 2, 13)
            )
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}

private class FakeDailyItemDaoForUpdate(
    initialItems: List<DailyItemEntity> = listOf(
        DailyItemEntity(id = 1L, dateKey = "2026-02-14", taskId = 10L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L)
    )
) : DailyItemDao {
    private val items = mutableMapOf<Long, DailyItemEntity>().apply {
        initialItems.forEach { put(it.id, it) }
    }
    private var idSeq: Long = (items.keys.maxOrNull() ?: 0L) + 1L

    override fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>> = flowOf(items.values.filter { it.dateKey == dateKey })
    override fun observeTodayTasks(dateKey: String) = flowOf(emptyList<com.dayquest.app.data.local.projection.TodayTaskRow>())

    override suspend fun insert(item: DailyItemEntity): Long {
        val exists = items.values.any { it.dateKey == item.dateKey && it.taskId == item.taskId }
        if (exists) return -1L

        val id = if (item.id > 0L) item.id else idSeq++
        items[id] = item.copy(id = id)
        return id
    }

    override suspend fun insertAll(items: List<DailyItemEntity>): List<Long> = items.map { insert(it) }

    override suspend fun update(item: DailyItemEntity) {
        this.items[item.id] = item
    }

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
    override fun observeCountByDate(dateKey: String): Flow<Int> = flowOf(items.values.count { it.dateKey == dateKey })

    override suspend fun countByDateAndStatus(dateKey: String, status: DailyItemStatus): Int =
        items.values.count { it.dateKey == dateKey && it.status == status }
    override fun observeCountByDateAndStatus(dateKey: String, status: DailyItemStatus): Flow<Int> =
        flowOf(items.values.count { it.dateKey == dateKey && it.status == status })
    override fun observeCountByDateRangeAndStatus(startDateKey: String, endDateKey: String, status: DailyItemStatus): Flow<Int> =
        flowOf(items.values.count { it.dateKey in startDateKey..endDateKey && it.status == status })
    override fun observeCountByDateRange(startDateKey: String, endDateKey: String): Flow<Int> =
        flowOf(items.values.count { it.dateKey in startDateKey..endDateKey })
    override fun observeDailyProgressByDateRange(startDateKey: String, endDateKey: String): Flow<List<com.dayquest.app.data.local.projection.HistoryDailyProgressRow>> =
        flowOf(emptyList())

    override suspend fun countImportantByDateAndStatus(dateKey: String, status: DailyItemStatus): Int = 0

    fun findByDateAndTask(dateKey: String, taskId: Long): DailyItemEntity? =
        items.values.firstOrNull { it.dateKey == dateKey && it.taskId == taskId }

    fun countByDateAndTask(dateKey: String, taskId: Long): Int =
        items.values.count { it.dateKey == dateKey && it.taskId == taskId }
}
