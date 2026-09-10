package com.example.taskpulse.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskpulse.worker.MarkTaskDoneWorker
import com.example.taskpulse.worker.ReminderWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class TaskRepository(private val taskDao: TaskDao, private val context: Context) {
    val allTask: Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun insert(task: Task): Long = taskDao.insert(task)
    suspend fun delete(task: Task) = taskDao.deleteTask(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun update(task: Task) = updateTask(task)
    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    val workManager = WorkManager.getInstance(context)

    // Constraints: only fire reminder when device is not in low battery / battery-saver mode
    private val reminderConstraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    fun scheduleReminder(
        taskId: Long,
        taskTitle: String,
        delayMinutes: Long,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        val data = createWorkerData(taskTitle, taskId)
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setConstraints(reminderConstraints)
            .setInitialDelay(delayMinutes, timeUnit = TimeUnit.MINUTES)
            .addTag(taskId.toString())
            .build()
        workManager.enqueueUniqueWork("reminder_$taskId", policy, workRequest)
    }

    fun createWorkerData(taskTitle: String, taskId: Long): Data {
        return Data.Builder()
            .putString(ReminderWorker.TASK_TITLE_KEY, taskTitle)
            .putLong(ReminderWorker.TASK_ID_KEY, taskId)
            .build()
    }

    fun scheduleRecurringReminder(
        taskId: Long,
        taskTitle: String,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        val data = createWorkerData(taskTitle, taskId)
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInputData(data)
            .setConstraints(reminderConstraints)
            .addTag(taskId.toString())
            .build()
        workManager.enqueueUniquePeriodicWork("recurring_reminder_$taskId", policy, workRequest)
    }

    fun cancelReminder(taskId: Long) {
        workManager.cancelAllWorkByTag(taskId.toString())
    }

    suspend fun markTaskCompleted(taskId: Long, isCompleted: Boolean = true) {
        taskDao.updateTaskCompletion(taskId, isCompleted)
        if (isCompleted) {
            cancelReminder(taskId)
        }
    }

    fun enqueueMarkTaskDone(taskId: Long) {
        val data = Data.Builder()
            .putLong(MarkTaskDoneWorker.TASK_ID_KEY, taskId)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<MarkTaskDoneWorker>()
            .setInputData(data)
            .build()
        workManager.enqueue(workRequest)
    }
}