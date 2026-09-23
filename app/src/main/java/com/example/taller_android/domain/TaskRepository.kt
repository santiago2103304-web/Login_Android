package com.example.taller_android.domain

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun publishTask(task: Task)
    suspend fun updateRemoteTask(task: Task)
    suspend fun deleteRemoteTask(task: Task)
    fun getRemoteTasks(userId: String): Flow<List<Task>>
}
