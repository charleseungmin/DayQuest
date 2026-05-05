package com.dayquest.app.ui.screen

import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import com.dayquest.app.domain.usecase.task.DeleteManageTaskUseCase
import com.dayquest.app.domain.usecase.task.ObserveManageTasksUseCase
import com.dayquest.app.domain.usecase.task.SaveManageTaskUseCase
import com.dayquest.app.ui.model.TaskManageUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek

class TaskManageViewModelRepeatTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `weekly repeat day toggle never leaves zero mask`() = runBlocking {
        val repository = RepeatFakeTaskRepository(
            listOf(
                TaskEntity(id = 1L, title = "운동", createdAtEpochMillis = 0L, updatedAtEpochMillis = 0L)
            )
        )
        val viewModel = TaskManageViewModel(
            observeManageTasksUseCase = ObserveManageTasksUseCase(repository),
            saveManageTaskUseCase = SaveManageTaskUseCase(repository),
            deleteManageTaskUseCase = DeleteManageTaskUseCase(repository)
        )

        delay(50)
        viewModel.updateRepeatType(RepeatType.WEEKLY)
        viewModel.toggleRepeatDay(DayOfWeek.MONDAY)

        val state = viewModel.uiState.value as TaskManageUiState.Ready
        assertEquals(RepeatType.WEEKLY, state.form.repeatType)
        assertEquals(1, state.form.repeatDaysMask)
    }
}

private class RepeatFakeTaskRepository(initial: List<TaskEntity>) : TaskRepository {
    private val tasksFlow = MutableStateFlow(initial)

    override fun observeActiveTasks(): Flow<List<TaskEntity>> = tasksFlow

    override suspend fun getActiveTasks(): List<TaskEntity> = tasksFlow.value

    override suspend fun getTask(taskId: Long): TaskEntity? = tasksFlow.value.firstOrNull { it.id == taskId }

    override suspend fun insert(task: TaskEntity): Long {
        val newId = (tasksFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        tasksFlow.value = tasksFlow.value + task.copy(id = newId)
        return newId
    }

    override suspend fun update(task: TaskEntity) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun softDelete(taskId: Long, updatedAt: Long) {
        tasksFlow.value = tasksFlow.value.filterNot { it.id == taskId }
    }
}
