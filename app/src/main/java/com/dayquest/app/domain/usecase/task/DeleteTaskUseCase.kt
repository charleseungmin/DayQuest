package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, updatedAtEpochMillis: Long) {
        require(taskId > 0L) { "Task id must be positive" }
        taskRepository.deleteTask(taskId, updatedAtEpochMillis)
    }
}
