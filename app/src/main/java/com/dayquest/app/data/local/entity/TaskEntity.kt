package com.dayquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.core.model.TaskType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
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
