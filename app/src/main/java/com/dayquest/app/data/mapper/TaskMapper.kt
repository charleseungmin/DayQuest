package com.dayquest.app.data.mapper

import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    taskType = taskType,
    priority = priority,
    repeatType = repeatType,
    repeatDaysMask = repeatDaysMask,
    isImportant = isImportant,
    targetTimeEpochMillis = targetTimeEpochMillis,
    isActive = isActive,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    taskType = taskType,
    priority = priority,
    repeatType = repeatType,
    repeatDaysMask = repeatDaysMask,
    isImportant = isImportant,
    targetTimeEpochMillis = targetTimeEpochMillis,
    isActive = isActive,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)
