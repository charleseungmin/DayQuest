package com.dayquest.app.domain.usecase.task

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
    fun `taskId가 null이면 새 Task를 추가한다`() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(taskId = null, title = "운동", category = "건강", isImportant = true, now = 1000L)

        assertEquals(SaveManageTaskResult.Created, result)
        assertEquals(1, repository.inserted.size)
        assertEquals("운동", repository.inserted.first().title)
        assertEquals("건강", repository.inserted.first().description)
        assertEquals(true, repository.inserted.first().isImportant)
    }

    @Test
    fun `taskId가 있으면 기존 Task를 수정한다`() = runBlocking {
        val repository = FakeTaskRepository(
            tasks = mutableMapOf(
                1L to TaskEntity(
                    id = 1L,
                    title = "기존",
                    description = "일반",
                    createdAtEpochMillis = 10L,
                    updatedAtEpochMillis = 10L
                )
            )
        )
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(taskId = 1L, title = "수정됨", category = "업무", isImportant = true, now = 2000L)

        assertEquals(SaveManageTaskResult.Updated, result)
        val updated = repository.updated.singleOrNull()
        assertNotNull(updated)
        assertEquals("수정됨", updated?.title)
        assertEquals("업무", updated?.description)
        assertEquals(true, updated?.isImportant)
        assertEquals(2000L, updated?.updatedAtEpochMillis)
    }

    @Test
    fun `수정 대상이 없으면 MissingTarget을 반환하고 폼 저장을 시도하지 않는다`() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(taskId = 99L, title = "없는 할일", category = "일반", isImportant = false, now = 3000L)

        assertEquals(SaveManageTaskResult.MissingTarget, result)
        assertEquals(0, repository.inserted.size)
        assertEquals(0, repository.updated.size)
    }

    @Test
    fun `새 할 일 제목이 기존 활성 할 일과 중복되면 DuplicateTitle을 반환한다`() = runBlocking {
        val repository = FakeTaskRepository(
            tasks = mutableMapOf(
                1L to TaskEntity(id = 1L, title = "운동", description = "건강", createdAtEpochMillis = 1L, updatedAtEpochMillis = 1L)
            )
        )
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(taskId = null, title = " 운동 ", category = "기타", isImportant = false, now = 4000L)

        assertEquals(SaveManageTaskResult.DuplicateTitle, result)
        assertEquals(0, repository.inserted.size)
    }

    @Test
    fun `수정 시 자기 자신 제목은 중복으로 보지 않는다`() = runBlocking {
        val repository = FakeTaskRepository(
            tasks = mutableMapOf(
                1L to TaskEntity(id = 1L, title = "운동", description = "건강", createdAtEpochMillis = 1L, updatedAtEpochMillis = 1L)
            )
        )
        val useCase = SaveManageTaskUseCase(repository)

        val result = useCase(taskId = 1L, title = "운동", category = "건강", isImportant = true, now = 5000L)

        assertEquals(SaveManageTaskResult.Updated, result)
        assertEquals(1, repository.updated.size)
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
