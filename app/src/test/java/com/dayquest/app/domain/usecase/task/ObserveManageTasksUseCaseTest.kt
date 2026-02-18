package com.dayquest.app.domain.usecase.task

import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveManageTasksUseCaseTest {

    @Test
    fun `Repository의 활성 Task Flow를 그대로 전달한다`() = runBlocking {
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "운동",
                description = "건강",
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L
            )
        )
        val repository = ObserveFakeTaskRepository(tasks)
        val useCase = ObserveManageTasksUseCase(repository)

        val result = useCase().first()

        assertEquals(tasks, result)
    }
}

private class ObserveFakeTaskRepository(
    private val tasks: List<TaskEntity>
) : TaskRepository {
    override fun observeActiveTasks(): Flow<List<TaskEntity>> = flowOf(tasks)

    override suspend fun getActiveTasks(): List<TaskEntity> = tasks

    override suspend fun getTask(taskId: Long): TaskEntity? = null

    override suspend fun insert(task: TaskEntity): Long = 0L

    override suspend fun update(task: TaskEntity) = Unit

    override suspend fun softDelete(taskId: Long, updatedAt: Long) = Unit
}
