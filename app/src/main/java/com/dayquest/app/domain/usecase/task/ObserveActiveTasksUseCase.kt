package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.model.Task
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeActiveTasks()
}
