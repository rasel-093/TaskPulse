package com.example.taskpulse.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskpulse.R

const val CHANNEL_ID = "TASK_REMINDER_CHANNEL"
const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task Notifications",
            NotificationManager.IMPORTANCE_HIGH
           // NotificationManager.IMPOR
        ).apply {
            description = "Notifications for task"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun sendNotification(message: String?, context: Context) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification)
        .setContentTitle("Task Reminder")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
