package com.dayquest.app.domain.model

import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.core.model.TaskType

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val taskType: TaskType = TaskType.ROUTINE,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val repeatType: RepeatType = RepeatType.DAILY,
    val repeatDaysMask: Int? = null,
    val isImportant: Boolean = false,
    val targetTimeEpochMillis: Long? = null,
    val isActive: Boolean = true,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
