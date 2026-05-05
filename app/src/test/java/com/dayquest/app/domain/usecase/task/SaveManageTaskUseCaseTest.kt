package com.dayquest.app.domain.usecase.task

import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SaveManageTaskUseCaseTest {

    @Test
    fun `creates a new task when taskId is null`() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(
            taskId = null,
            title = "Workout",
            category = "Health",
            priority = TaskPriority.HIGH,
            isImportant = true,
            repeatType = RepeatType.CUSTOM,
            repeatDaysMask = 42,
            now = 1000L
        )

        assertEquals(SaveManageTaskResult.Created, result)
        assertEquals(1, repository.inserted.size)
        assertEquals(RepeatType.CUSTOM, repository.inserted.first().repeatType)
        assertEquals(42, repository.inserted.first().repeatDaysMask)
    }

    @Test
    fun `updates existing task when taskId exists`() = runBlocking {
        val repository = FakeTaskRepository(
            tasks = mutableMapOf(
                1L to TaskEntity(id = 1L, title = "Legacy", description = "General", createdAtEpochMillis = 10L, updatedAtEpochMillis = 10L)
            )
        )
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(
            taskId = 1L,
            title = "Updated",
            category = "Work",
            priority = TaskPriority.LOW,
            isImportant = true,
            repeatType = RepeatType.MONTHLY,
            repeatDaysMask = 64,
            now = 2000L
        )

        assertEquals(SaveManageTaskResult.Updated, result)
        val updated = repository.updated.singleOrNull()
        assertNotNull(updated)
        assertEquals(RepeatType.MONTHLY, updated?.repeatType)
        assertEquals(null, updated?.repeatDaysMask)
    }

    @Test
    fun `uses monday default repeat mask when weekly repeat days are missing`() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(
            taskId = null,
            title = "Daily standup",
            category = "Work",
            priority = TaskPriority.MEDIUM,
            isImportant = false,
            repeatType = RepeatType.WEEKLY,
            repeatDaysMask = null,
            now = 3000L
        )

        assertEquals(SaveManageTaskResult.Created, result)
        assertEquals(1, repository.inserted.size)
        assertEquals(1, repository.inserted.first().repeatDaysMask)
    }
}

private class FakeTaskRepository(
    val tasks: MutableMap<Long, TaskEntity> = mutableMapOf()
) : TaskRepository {
    val inserted = mutableListOf<TaskEntity>()
    val updated = mutableListOf<TaskEntity>()

    override fun observeActiveTasks(): Flow<List<TaskEntity>> = flowOf(tasks.values.toList())
    override suspend fun getActiveTasks(): List<TaskEntity> = tasks.values.toList()
    override suspend fun getTask(taskId: Long): TaskEntity? = tasks[taskId]

    override suspend fun insert(task: TaskEntity): Long {
        inserted += task
        val newId = (tasks.keys.maxOrNull() ?: 0L) + 1L
        tasks[newId] = task.copy(id = newId)
        return newId
    }

    override suspend fun update(task: TaskEntity) {
        updated += task
        tasks[task.id] = task
    }

    override suspend fun softDelete(taskId: Long, updatedAt: Long) {
        tasks.remove(taskId)
    }
}
