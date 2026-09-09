package com.example.taskpulse.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskpulse.worker.ReminderWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class TaskRepository(private val taskDao: TaskDao, private val context: Context) {
    val allTask: Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun insert(task: Task): Long = taskDao.insert(task)
    suspend fun delete(task: Task) = taskDao.deleteTask(task)
    val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(taskId: Long, taskTitle: String, delayMinutes: Long) {
        val data = createWorkerData(taskTitle)
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMinutes, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
            .addTag(taskId.toString())
            .build()
        workManager.enqueueUniqueWork("reminder_$taskId", ExistingWorkPolicy.KEEP, workRequest)
    }

    fun createWorkerData(taskTitle: String): Data{
        return Data.Builder()
            .putString(ReminderWorker.TASK_TITLE_KEY, taskTitle)
            .build()
    }
    fun scheduleRecurringReminder(taskId: Long, taskTitle: String) {
        val data = createWorkerData(taskTitle)
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInputData(data)
            .addTag(taskId.toString())
            .build()
        workManager.enqueueUniquePeriodicWork("recurring_reminder_$taskId", ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }
    fun cancelReminder(taskId: Long) {
        workManager.cancelAllWorkByTag(taskId.toString())
    }
}