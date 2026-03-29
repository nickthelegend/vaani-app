package com.vaani.app.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.vaani.app.data.models.ActionType
import com.vaani.app.data.models.AgentAction
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

class VaaniAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VaaniAccessibility"
        var instance: VaaniAccessibilityService? = null
        val isRunning = MutableStateFlow(false)
        val currentPackage = MutableStateFlow("")

        fun getService(): VaaniAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        instance = this
        isRunning.value = true
        
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        event.packageName?.let {
            currentPackage.value = it.toString()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        isRunning.value = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        instance = null
        isRunning.value = false
        return super.onUnbind(intent)
    }

    fun getScreenTreeJson(): String {
        val root = rootInActiveWindow ?: return "{}"
        return ScreenReader.serializeNode(root, 0)
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                true
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch app: $packageName", e)
            false
        }
    }

    fun pressBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    fun pressHome() = performGlobalAction(GLOBAL_ACTION_HOME)
}
