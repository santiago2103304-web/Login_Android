package com.example.taller_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_android.domain.Task
import com.example.taller_android.domain.TaskUseCases
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de la gestión del estado reactivo de las tareas (locales y remotas).
 *
 * @author Santiago
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    private val auth: FirebaseAuth
) : ViewModel() {

    val drafts: StateFlow<List<Task>> = useCases.getDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val remoteTasks: StateFlow<List<Task>> = auth.currentUser?.uid?.let { userId ->
        useCases.getRemoteTasks(userId)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) ?: MutableStateFlow(emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun saveTask(
        title: String,
        description: String,
        priority: String,
        isDraft: Boolean,
        taskToEdit: Task? = null
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                if (taskToEdit != null) {
                    val updatedTask = taskToEdit.copy(
                        title = title,
                        description = description,
                        priority = priority
                    )
                    if (taskToEdit.isDraft) {
                        useCases.updateDraft(updatedTask)
                    } else {
                        useCases.updateRemoteTask(updatedTask)
                    }
                } else {
                    val newTask = Task(
                        title = title,
                        description = description,
                        priority = priority,
                        userId = userId,
                        isDraft = isDraft
                    )
                    if (isDraft) {
                        useCases.saveDraft(newTask)
                    } else {
                        useCases.publishTask(newTask)
                    }
                }
            }.onFailure {
                _errorMessage.value = "Error al guardar la tarea: ${it.localizedMessage}"
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            runCatching {
                val updated = task.copy(isCompleted = !task.isCompleted)
                if (task.isDraft) {
                    useCases.updateDraft(updated)
                } else {
                    useCases.updateRemoteTask(updated)
                }
            }.onFailure {
                _errorMessage.value = "Error al actualizar tarea: ${it.localizedMessage}"
            }
        }
    }

    fun publishTask(task: Task) = viewModelScope.launch {
        runCatching { useCases.publishTask(task) }.onFailure {
            _errorMessage.value = "Error al publicar: ${it.localizedMessage}"
        }
    }
    fun deleteDraft(task: Task) = viewModelScope.launch {
        runCatching { useCases.deleteDraft(task) }.onFailure {
            _errorMessage.value = "Error al eliminar borrador: ${it.localizedMessage}"
        }
    }
    fun deleteRemoteTask(task: Task) = viewModelScope.launch {
        runCatching { useCases.deleteRemoteTask(task) }.onFailure {
            _errorMessage.value = "Error al eliminar en Firebase: ${it.localizedMessage}"
        }
    }
}
