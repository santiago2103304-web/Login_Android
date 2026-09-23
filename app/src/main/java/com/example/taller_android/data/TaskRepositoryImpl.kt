package com.example.taller_android.data

import com.example.taller_android.domain.Task
import com.example.taller_android.domain.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * Implementación del repositorio de tareas administrando el almacenamiento híbrido (Room local + Cloud Firestore).
 *
 * @author Santiago
 */
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val firestore: FirebaseFirestore
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> = dao.getTasks().map { list -> list.map { it.toTask() } }

    override suspend fun getTaskById(id: String): Task? = dao.getTaskById(id)?.toTask()

    override suspend fun insertTask(task: Task) {
        val id = task.id.ifEmpty { UUID.randomUUID().toString() }
        dao.insertTask(task.copy(id = id).toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) = dao.deleteTask(task.toEntity())

    override suspend fun publishTask(task: Task) {
        val id = task.id.ifEmpty { UUID.randomUUID().toString() }
        val taskToPublish = task.copy(id = id, isDraft = false)
        firestore.collection("tasks").document(id).set(taskToPublish).await()
        if (task.isDraft && task.id.isNotEmpty()) {
            dao.deleteTask(task.toEntity())
        }
    }

    override suspend fun updateRemoteTask(task: Task) {
        if (task.id.isNotEmpty()) {
            firestore.collection("tasks").document(task.id).set(task.copy(isDraft = false)).await()
        }
    }

    override suspend fun deleteRemoteTask(task: Task) {
        if (task.id.isNotEmpty()) {
            firestore.collection("tasks").document(task.id).delete().await()
        }
    }

    override fun getRemoteTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val sub = firestore.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { s, err ->
                if (err != null) return@addSnapshotListener
                s?.let {
                    val tasks = it.toObjects(Task::class.java)
                    trySend(tasks)
                }
            }
        awaitClose { sub.remove() }
    }
}
