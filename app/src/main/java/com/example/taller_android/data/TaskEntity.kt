package com.example.taller_android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taller_android.domain.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val isCompleted: Boolean,
    val userId: String,
    val createdAt: Long
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description,
        priority = priority,
        isCompleted = isCompleted,
        isDraft = true,
        userId = userId,
        createdAt = createdAt
    )
}

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority,
    isCompleted = isCompleted,
    userId = userId,
    createdAt = createdAt
)
