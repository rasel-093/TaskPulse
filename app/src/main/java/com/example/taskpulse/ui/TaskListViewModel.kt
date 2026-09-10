package com.example.taskpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.Task
import com.example.taskpulse.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(val repository: TaskRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.allTask.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Expose repository.scheduleReminder to the UI layer
     */
    fun scheduleReminder(taskId: Long, taskTitle: String, delayMinutes: Long) {
        repository.scheduleReminder(taskId, taskTitle, delayMinutes)
    }

    fun scheduleReminder(task: Task) {
        scheduleReminder(task.id, task.title, task.dueMinutes)
    }

    fun scheduleRecurringReminder(taskId: Long, taskTitle: String) {
        repository.scheduleRecurringReminder(taskId, taskTitle)
    }

    fun cancelReminder(taskId: Long) {
        repository.cancelReminder(taskId)
    }

    suspend fun insertTask(task: Task): Long {
        return repository.insert(task)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.cancelReminder(task.id)
            repository.delete(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.markTaskCompleted(task.id, newStatus)
        }
    }

    fun markTaskDone(taskId: Long) {
        viewModelScope.launch {
            repository.markTaskCompleted(taskId, true)
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                return TaskListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
