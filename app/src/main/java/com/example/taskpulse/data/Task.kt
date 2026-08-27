package com.example.taskpulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: Long = 0,
    val title: String,
    val dueMinutes: Long,
    val isRecurring: Boolean = false
)