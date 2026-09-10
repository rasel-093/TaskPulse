package com.example.taskpulse.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskpulse.receiver.ReminderReceiver
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
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val reminderConstraints = Constraints.Builder().build()

    fun scheduleReminder(
        taskId: Long,
        taskTitle: String,
        delayMinutes: Long,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        // 1. Schedule exact wake-up alarm with AlarmManager so 1m / exact reminders trigger reliably in background
        if (alarmManager != null && delayMinutes > 0L) {
            val alarmIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
                putExtra(ReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // If permission missing, fallback to setAndAllowWhileIdle or WorkManager
                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } catch (e2: Exception) {
                    // Handled by WorkManager below
                }
            }
        }

        // 2. Also keep WorkManager work request for fallback / compatibility
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
        // Cancel exact alarm
        try {
            val alarmIntent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Cancel WorkManager work
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