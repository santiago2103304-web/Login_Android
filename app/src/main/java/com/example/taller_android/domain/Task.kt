package com.example.taller_android.domain

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = "Media",
    val isCompleted: Boolean = false,
    val isDraft: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
