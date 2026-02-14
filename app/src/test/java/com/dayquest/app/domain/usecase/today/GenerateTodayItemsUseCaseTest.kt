package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.usecase.task.ShouldGenerateTaskForDateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class GenerateTodayItemsUseCaseTest {

    @Test
    fun generate_today_items_inserts_matching_tasks_once() = runBlocking {
        val mondayMask = ShouldGenerateTaskForDateUseCase.makeWeeklyMask(DayOfWeek.MONDAY)
        val baseDate = LocalDate.of(2026, 2, 1)
        val baseMillis = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val taskDao = FakeTaskDaoForGenerate(
            listOf(
                TaskEntity(id = 1, title = "Daily", repeatType = RepeatType.DAILY, createdAtEpochMillis = baseMillis, updatedAtEpochMillis = baseMillis),
                TaskEntity(id = 2, title = "Weekly Mon", repeatType = RepeatType.WEEKLY, repeatDaysMask = mondayMask, createdAtEpochMillis = baseMillis, updatedAtEpochMillis = baseMillis),
                TaskEntity(id = 3, title = "Monthly 1st", repeatType = RepeatType.MONTHLY, createdAtEpochMillis = baseMillis, updatedAtEpochMillis = baseMillis)
            )
        )
        val dailyDao = FakeDailyItemDaoForGenerate()
        val useCase = GenerateTodayItemsUseCase(taskDao, dailyDao, ShouldGenerateTaskForDateUseCase())

        val generated = useCase(LocalDate.of(2026, 2, 2), 1000L) // Monday
        assertEquals(2, generated)

        val generatedAgain = useCase(LocalDate.of(2026, 2, 2), 2000L)
        assertEquals(0, generatedAgain)
    }
}

private class FakeTaskDaoForGenerate(
    private val tasks: List<TaskEntity>
) : TaskDao {
    override fun observeActiveTasks(): Flow<List<TaskEntity>> = flowOf(tasks)
    override suspend fun getActiveTasks(): List<TaskEntity> = tasks
    override suspend fun insert(task: TaskEntity): Long = 0
    override suspend fun update(task: TaskEntity) = Unit
    override suspend fun softDelete(taskId: Long, updatedAt: Long) = Unit
    override suspend fun getById(taskId: Long): TaskEntity? = tasks.firstOrNull { it.id == taskId }
}

private class FakeDailyItemDaoForGenerate : DailyItemDao {
    private val items = mutableListOf<DailyItemEntity>()

    override fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>> = flowOf(items.filter { it.dateKey == dateKey })
    override suspend fun insert(item: DailyItemEntity): Long { items += item; return item.id }

    override suspend fun insertAll(items: List<DailyItemEntity>): List<Long> {
        val results = mutableListOf<Long>()
        items.forEach { incoming ->
            val exists = this.items.any { it.dateKey == incoming.dateKey && it.taskId == incoming.taskId }
            if (exists) results += -1L else {
                this.items += incoming
                results += this.items.size.toLong()
            }
        }
        return results
    }

    override suspend fun update(item: DailyItemEntity) = Unit
    override suspend fun getById(id: Long): DailyItemEntity? = items.firstOrNull { it.id == id }
    override suspend fun updateState(id: Long, status: DailyItemStatus, completedAt: Long?, deferredToDateKey: String?) = Unit
    override suspend fun countByDate(dateKey: String): Int = items.count { it.dateKey == dateKey }
}
