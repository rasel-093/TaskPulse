package com.example.taskpulse.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskpulse.MainActivity
import com.example.taskpulse.R

const val CHANNEL_ID = "TASK_REMINDER_CHANNEL"
const val ACTION_MARK_DONE = "com.example.taskpulse.ACTION_MARK_DONE"
const val ACTION_SNOOZE = "com.example.taskpulse.ACTION_SNOOZE"
const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"

// Brand Indigo accent color for notification chrome (#3C4A6B)
private const val NOTIFICATION_ACCENT_COLOR = 0xFF3C4A6B.toInt()

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for scheduled tasks and reminders"
            enableLights(true)
            lightColor = NOTIFICATION_ACCENT_COLOR
            enableVibration(true)
            setShowBadge(true)
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun sendNotification(message: String, context: Context, taskId: Long) {
    // 1. Content Intent: opens app when notification body is clicked
    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_TASK_ID, taskId)
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        taskId.toInt(),
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 2. Action: Mark Done
    val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_MARK_DONE
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_TASK_TITLE, message)
    }
    val donePendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        doneIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 3. Action: Snooze (distinct requestCode prevents collision with donePendingIntent)
    val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_SNOOZE
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_TASK_TITLE, message)
    }
    val snoozePendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt() + 100_000,
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Expandable BigTextStyle matching Screen 03 of visual design reference
    val bigTextStyle = NotificationCompat.BigTextStyle()
        .setBigContentTitle(message)
        .bigText("Reminder due now. Tap Done to mark it complete, or Snooze to be reminded in 10 minutes.")
        .setSummaryText("TaskPulse")

    // Load rich full-color app icon for the notification card
    val appIconBitmap = try {
        androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?.let { androidx.core.graphics.drawable.DrawableCompat.wrap(it) }
            ?.let { drawable ->
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 128
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 128
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
    } catch (e: Exception) {
        null
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .apply {
            if (appIconBitmap != null) {
                setLargeIcon(appIconBitmap)
            }
        }
        .setContentTitle(message)
        .setContentText("Tap Done to mark complete, or Snooze 10m.")
        .setStyle(bigTextStyle)
        .setColor(NOTIFICATION_ACCENT_COLOR)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .addAction(
            android.R.drawable.ic_lock_silent_mode,
            "Snooze 10m",
            snoozePendingIntent
        )
        .addAction(
            android.R.drawable.ic_menu_send,
            "Done",
            donePendingIntent
        )
        .build()

    NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
}