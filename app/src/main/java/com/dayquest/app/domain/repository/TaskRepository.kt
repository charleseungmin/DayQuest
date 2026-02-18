package com.dayquest.app.domain.repository

import com.dayquest.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeActiveTasks(): Flow<List<TaskEntity>>
    suspend fun getActiveTasks(): List<TaskEntity>
    suspend fun getTask(taskId: Long): TaskEntity?
    suspend fun insert(task: TaskEntity): Long
    suspend fun update(task: TaskEntity)
    suspend fun softDelete(taskId: Long, updatedAt: Long)
}
