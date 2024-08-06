package com.example.be_bored_compose

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel("alerts", "Alerts", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}