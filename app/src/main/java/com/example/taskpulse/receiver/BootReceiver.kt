package com.example.taskpulse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taskpulse.data.TaskDatabase
import com.example.taskpulse.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TaskDatabase.getDatabase(context)
                val repository = TaskRepository(database.taskDao(), context)
                val tasks = repository.allTask.first()

                for (task in tasks) {
                    if (!task.isCompleted) {
                        if (task.isRecurring) {
                            repository.scheduleRecurringReminder(task.id, task.title)
                        } else if (task.dueMinutes > 0L) {
                            // Reschedule active non-completed tasks
                            repository.scheduleReminder(task.id, task.title, task.dueMinutes)
                        }
                    }
                }
            } catch (e: Exception) {
                // Log and gracefully handle
            } finally {
                pendingResult.finish()
            }
        }
    }
}
