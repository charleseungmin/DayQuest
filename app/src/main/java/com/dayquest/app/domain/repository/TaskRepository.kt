package com.dayquest.app.domain.repository

import com.dayquest.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeActiveTasks(): Flow<List<Task>>
    suspend fun getTask(taskId: Long): Task?
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long, updatedAt: Long)
}
