package com.vaani.app.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            Log.d("BootReceiver", "Starting Vaani services after boot/update")
            
            val serviceIntent = Intent(context, VaaniForegroundService::class.java)
            context.startForegroundService(serviceIntent)
            
            // Note: Accessibility service is handled by the system based on user settings
        }
    }
}
