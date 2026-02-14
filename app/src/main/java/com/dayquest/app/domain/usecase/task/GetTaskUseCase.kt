package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.model.Task
import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Task? {
        require(taskId > 0L) { "Task id must be positive" }
        return taskRepository.getTask(taskId)
    }
}
