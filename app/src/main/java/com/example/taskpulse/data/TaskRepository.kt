package com.example.taskpulse.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskpulse.worker.ReminderWorker
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao, private val context: Context) {
    val allTask: Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun insert(task: Task): Long = taskDao.insert(task)
    suspend fun delete(task: Task) = taskDao.deleteTask(task)

    fun scheduleReminder(taskId: Long, taskTitle: String, delayMinutes: Long) {
        val data = createWorkerData(taskTitle)
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMinutes, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
            .addTag(taskId.toString())
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork("reminder_$taskId", ExistingWorkPolicy.KEEP, workRequest)
    }

    fun createWorkerData(taskTitle: String): Data{
        return Data.Builder()
            .putString(ReminderWorker.TASK_TITLE_KEY, taskTitle)
            .build()
    }
}