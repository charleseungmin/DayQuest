package com.dayquest.app.domain.usecase.task

import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import javax.inject.Inject

class SaveManageTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    private companion object {
        const val DEFAULT_REPEAT_DAYS_MASK = 1 // Monday
    }

    suspend operator fun invoke(
        taskId: Long?,
        title: String,
        category: String,
        priority: TaskPriority,
        isImportant: Boolean,
        repeatType: RepeatType,
        repeatDaysMask: Int?,
        now: Long
    ): SaveManageTaskResult {
        val normalizedTitle = title.trim().lowercase()
        val hasDuplicateTitle = taskRepository
            .getActiveTasks()
            .any { it.id != taskId && it.title.trim().lowercase() == normalizedTitle }
        if (hasDuplicateTitle) {
            return SaveManageTaskResult.DuplicateTitle
        }

        val normalizedRepeatDaysMask = when (repeatType) {
            RepeatType.WEEKLY,
            RepeatType.CUSTOM -> repeatDaysMask ?: DEFAULT_REPEAT_DAYS_MASK
            else -> null
        }

        if (taskId == null) {
            taskRepository.insert(
                TaskEntity(
                    title = title,
                    description = category,
                    priority = priority,
                    isImportant = isImportant,
                    repeatType = repeatType,
                    repeatDaysMask = normalizedRepeatDaysMask,
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
                priority = priority,
                isImportant = isImportant,
                repeatType = repeatType,
                repeatDaysMask = normalizedRepeatDaysMask,
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
