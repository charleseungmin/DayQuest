package com.dayquest.app.data.repository

import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun observeActiveTasks(): Flow<List<TaskEntity>> = taskDao.observeActiveTasks()

    override suspend fun getTask(taskId: Long): TaskEntity? = taskDao.getById(taskId)

    override suspend fun insert(task: TaskEntity): Long = taskDao.insert(task)

    override suspend fun update(task: TaskEntity) = taskDao.update(task)

    override suspend fun softDelete(taskId: Long, updatedAt: Long) = taskDao.softDelete(taskId, updatedAt)
}
