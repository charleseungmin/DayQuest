package com.dayquest.app.domain.usecase.task

import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class SaveManageTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long?, title: String, category: String, isImportant: Boolean, now: Long): SaveManageTaskResult {
        val normalizedTitle = title.trim().lowercase()
        val hasDuplicateTitle = taskRepository
            .getActiveTasks()
            .any { it.id != taskId && it.title.trim().lowercase() == normalizedTitle }
        if (hasDuplicateTitle) {
            return SaveManageTaskResult.DuplicateTitle
        }

        if (taskId == null) {
            taskRepository.insert(
                TaskEntity(
                    title = title,
                    description = category,
                    isImportant = isImportant,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now
                )
            )
            return SaveManageTaskResult.Created
        }

        val existing = taskRepository.getTask(taskId) ?: return SaveManageTaskResult.MissingTarget
        taskRepository.update(
            existing.copy(
                title = title,
                description = category,
                isImportant = isImportant,
                updatedAtEpochMillis = now
            )
        )
        return SaveManageTaskResult.Updated
    }
}

enum class SaveManageTaskResult {
    Created,
    Updated,
    MissingTarget,
    DuplicateTitle
}
