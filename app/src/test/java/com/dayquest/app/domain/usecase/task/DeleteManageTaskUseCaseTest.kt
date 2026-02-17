package com.dayquest.app.domain.usecase.task

import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteManageTaskUseCaseTest {

    @Test
    fun `삭제 요청 시 softDelete를 호출한다`() = runBlocking {
        val repository = DeleteFakeTaskRepository()
        val useCase = DeleteManageTaskUseCase(repository)

        useCase(taskId = 7L, now = 1234L)

        assertEquals(7L, repository.deletedTaskId)
        assertEquals(1234L, repository.deletedUpdatedAt)
    }
}

private class DeleteFakeTaskRepository : TaskRepository {
    var deletedTaskId: Long? = null
    var deletedUpdatedAt: Long? = null

    override fun observeActiveTasks(): Flow<List<TaskEntity>> = flowOf(emptyList())

    override suspend fun getTask(taskId: Long): TaskEntity? = null

    override suspend fun insert(task: TaskEntity): Long = 0L

    override suspend fun update(task: TaskEntity) = Unit

    override suspend fun softDelete(taskId: Long, updatedAt: Long) {
        deletedTaskId = taskId
        deletedUpdatedAt = updatedAt
    }
}
