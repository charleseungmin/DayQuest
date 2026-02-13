package com.dayquest.app.data.repository

import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.mapper.toDomain
import com.dayquest.app.data.mapper.toEntity
import com.dayquest.app.domain.model.Task
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun observeActiveTasks(): Flow<List<Task>> =
        taskDao.observeActiveTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(taskId: Long): Task? = taskDao.getById(taskId)?.toDomain()

    override suspend fun createTask(task: Task): Long = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

    override suspend fun deleteTask(taskId: Long, updatedAt: Long) = taskDao.softDelete(taskId, updatedAt)
}
