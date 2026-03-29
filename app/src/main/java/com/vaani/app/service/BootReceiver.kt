package com.vaani.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting Vaani service")
            
            val prefs = context.getSharedPreferences("vaani_prefs", Context.MODE_PRIVATE)
            val autoStartEnabled = prefs.getBoolean("auto_start_enabled", false)
            
            if (autoStartEnabled) {
                VaaniService.startService(context)
            }
        }
    }
}
