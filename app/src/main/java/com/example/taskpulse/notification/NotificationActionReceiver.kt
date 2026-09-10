package com.example.taskpulse.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.taskpulse.data.TaskDatabase
import com.example.taskpulse.data.TaskRepository

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val database = TaskDatabase.getDatabase(context)
        val repository = TaskRepository(database.taskDao(), context)

        when (intent.action) {
            ACTION_MARK_DONE -> {
                // 1. Cancel the reminder work for this task
                repository.cancelReminder(taskId)

                // 2. Dismiss the notification from the shade
                NotificationManagerCompat.from(context).cancel(taskId.toInt())

                // 3. Enqueue work to mark task done in DB
                repository.enqueueMarkTaskDone(taskId)
            }
            ACTION_SNOOZE -> {
                // 1. Cancel current reminder, then reschedule a new one ~10 minutes from now
                repository.cancelReminder(taskId)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
                repository.scheduleReminder(taskId, taskTitle, 10L, androidx.work.ExistingWorkPolicy.REPLACE)

                // 2. Dismiss the current notification from the shade
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
            }
        }
    }
}