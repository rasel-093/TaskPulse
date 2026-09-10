package com.example.taskpulse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.taskpulse.notification.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.taskpulse.data.TaskDatabase

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"

        if (taskId == -1L) return

        // Cancel any corresponding WorkManager job so it does not fire again when the app is opened
        try {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
        } catch (e: Exception) {
            // WorkManager may not be initialized yet
        }

        // Verify task is not already completed in DB before notifying
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TaskDatabase.getDatabase(context)
                val task = database.taskDao().getTaskById(taskId)
                if (task != null && !task.isCompleted) {
                    sendNotification(taskTitle, context, taskId)
                } else if (task == null) {
                    // Fallback in case task lookup fails: send notification
                    sendNotification(taskTitle, context, taskId)
                }
            } catch (e: Exception) {
                // If DB check fails, notify user anyway so reminder isn't lost
                sendNotification(taskTitle, context, taskId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
