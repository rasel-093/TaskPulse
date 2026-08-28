package com.example.taskpulse

import android.app.Application
import com.example.taskpulse.notification.createNotificationChannel

class TaskPulseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
    }
}