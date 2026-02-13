package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.model.Task
import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        require(task.id > 0L) { "Task id must be positive" }
        require(task.title.isNotBlank()) { "Task title must not be blank" }
        taskRepository.updateTask(task)
    }
}
