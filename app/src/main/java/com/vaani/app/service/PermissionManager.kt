package com.vaani.app.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "vaani_permissions"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        
        const val REQUEST_CODE_MICROPHONE = 1001
        const val REQUEST_CODE_NOTIFICATION = 1002
        const val REQUEST_CODE_OVERLAY = 1003
        const val REQUEST_CODE_ACCESSIBILITY = 1004

        const val SETTINGS_ACTION_ACCESSIBILITY_SERVICE = "android.accessibilityservice.AccessibilitySettings"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class PermissionState(
        val microphone: Boolean = false,
        val notification: Boolean = false,
        val overlay: Boolean = false,
        val accessibility: Boolean = false
    )

    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasAccessibilityPermission(): Boolean {
        return VaaniAccessibilityService.isServiceRunning.value
    }

    fun getPermissionState(): PermissionState {
        return PermissionState(
            microphone = hasMicrophonePermission(),
            notification = hasNotificationPermission(),
            overlay = hasOverlayPermission(),
            accessibility = hasAccessibilityPermission()
        )
    }

    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    fun requestMicrophonePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_MICROPHONE
        )
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATION
            )
        }
    }

    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY)
    }

    fun openAccessibilitySettings(activity: Activity) {
        val intent = Intent(SETTINGS_ACTION_ACCESSIBILITY_SERVICE)
        activity.startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY)
    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        activity.startActivity(intent)
    }

    fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            Manifest.permission.RECORD_AUDIO -> "Vaani needs access to your microphone to hear your voice commands."
            Manifest.permission.POST_NOTIFICATIONS -> "Vaani needs notification permission to show when it's active."
            "overlay" -> "Vaani can show a floating button on top of other apps for easy access."
            "accessibility" -> "Vaani needs accessibility permission to control other apps and perform actions."
            else -> "This permission is required for Vaani to work properly."
        }
    }

    fun getAllRequiredPermissions(): List<String> {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }

    fun areAllEssentialPermissionsGranted(): Boolean {
        return hasMicrophonePermission() && hasAccessibilityPermission()
    }

    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()
        
        if (!hasMicrophonePermission()) {
            missing.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (!hasOverlayPermission()) {
            missing.add("overlay")
        }
        
        if (!hasAccessibilityPermission()) {
            missing.add("accessibility")
        }
        
        return missing
    }
}
