package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.model.Task
import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long {
        require(task.title.isNotBlank()) { "Task title must not be blank" }
        return taskRepository.createTask(task)
    }
}
