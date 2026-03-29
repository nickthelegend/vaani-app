package com.vaani.app.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vaani.app.R
import com.vaani.app.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaaniForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "vaani_channel")
            .setContentTitle("Vaani is Active")
            .setContentText("Tap to open Vaani")
            .setSmallIcon(R.drawable.ic_vaani)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
