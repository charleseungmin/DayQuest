package com.dayquest.app.domain.usecase.task

import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteManageTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, now: Long) {
        taskRepository.softDelete(taskId, now)
    }
}
