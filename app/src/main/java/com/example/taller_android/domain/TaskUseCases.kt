package com.example.taller_android.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskUseCases @Inject constructor(
    private val repository: TaskRepository
) {
    fun getDrafts(): Flow<List<Task>> = repository.getTasks()
    suspend fun saveDraft(task: Task) = repository.insertTask(task.copy(isDraft = true))
    suspend fun updateDraft(task: Task) = repository.updateTask(task)
    suspend fun deleteDraft(task: Task) = repository.deleteTask(task)

    suspend fun publishTask(task: Task) = repository.publishTask(task)
    suspend fun updateRemoteTask(task: Task) = repository.updateRemoteTask(task)
    suspend fun deleteRemoteTask(task: Task) = repository.deleteRemoteTask(task)

    fun getRemoteTasks(userId: String): Flow<List<Task>> = repository.getRemoteTasks(userId)
}
