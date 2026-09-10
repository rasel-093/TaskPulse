package com.example.taskpulse.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.jvm.java

const val CHANNEL_ID = "TASK_REMINDER_CHANNEL"
const val ACTION_MARK_DONE = "com.example.taskpulse.ACTION_MARK_DONE"
const val ACTION_SNOOZE = "com.example.taskpulse.ACTION_SNOOZE"
const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"

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

//@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//fun sendNotification(message: String?, context: Context) {
//    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//        .setSmallIcon(R.drawable.notification)
//        .setContentTitle("Task Reminder")
//        .setContentText(message)
//        .setPriority(NotificationCompat.PRIORITY_HIGH)
//        .setAutoCancel(true)
//        .build()
//
//    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
//}
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun sendNotification(message: String, context: Context, taskId: Long) {
    val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_MARK_DONE
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_TASK_TITLE, message)
    }
    val donePendingIntent = PendingIntent.getBroadcast(
        context, /* requestCode = */ taskId.toInt(), doneIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_SNOOZE
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_TASK_TITLE, message)
    }

    val snoozePendingIntent = PendingIntent.getBroadcast(
        context, taskId.toInt(), snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Task Reminder")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .addAction(android.R.drawable.ic_menu_send, "Done", donePendingIntent)
        .addAction(android.R.drawable.ic_lock_silent_mode, "Snooze", snoozePendingIntent)
        .build()
    NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
}