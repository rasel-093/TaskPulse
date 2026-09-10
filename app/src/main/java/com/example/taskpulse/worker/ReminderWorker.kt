package com.example.taskpulse.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.taskpulse.notification.sendNotification

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val permission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
        if ( permission == PackageManager.PERMISSION_GRANTED){
            val taskTitle = inputData.getString(TASK_TITLE_KEY) ?: return Result.failure()
            val taskId = inputData.getLong(TASK_ID_KEY, defaultValue = 0L)
            if (taskId == 0L) return Result.failure()
            sendNotification(
                context = applicationContext, message = taskTitle,
                taskId = taskId
            )
        }
        return Result.success()
    }
    companion object {
        const val TASK_TITLE_KEY = "TASK_TITLE_KEY"
        const val TASK_ID_KEY = "TASK_ID_KEY"
    }
}