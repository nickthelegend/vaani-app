package com.vaani.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.vaani.app.data.model.Bounds
import com.vaani.app.data.model.ScreenNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VaaniAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VaaniAccessibilityService"
        
        val screenState: StateFlow<ScreenNode?> get() = _screenState
        val isServiceRunning: StateFlow<Boolean> get() = _isServiceRunning
        val instance: VaaniAccessibilityService? get() = _instance
        
        private val _screenState = MutableStateFlow<ScreenNode?>(null)
        private val _isServiceRunning = MutableStateFlow(false)
        private var _instance: VaaniAccessibilityService? = null
        
        private val screenChangeListeners = mutableListOf<(ScreenReader.ScreenCapture) -> Unit>()
    }

    private var currentPackage: String? = null
    
    init {
        _instance = this
    }
    
    fun addScreenChangeListener(listener: (ScreenReader.ScreenCapture) -> Unit) {
        screenChangeListeners.add(listener)
    }
    
    fun removeScreenChangeListener(listener: (ScreenReader.ScreenCapture) -> Unit) {
        screenChangeListeners.remove(listener)
    }
    
    private fun notifyScreenChange(capture: ScreenReader.ScreenCapture) {
        screenChangeListeners.forEach { it(capture) }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
        _isServiceRunning.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            currentPackage = it.packageName?.toString()
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                val rootNode = rootInActiveWindow
                rootNode?.let { root ->
                    _screenState.value = buildScreenNode(root)
                    root.recycle()
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        _screenState.value = null
        Log.d(TAG, "Service destroyed")
    }

    override fun onMotionEvent(event: android.view.MotionEvent?) {
        super.onMotionEvent(event)
    }

    private fun buildScreenNode(node: AccessibilityNodeInfo, depth: Int = 0): ScreenNode {
        if (depth > 20) return ScreenNode(
            resourceId = null,
            text = null,
            contentDescription = null,
            className = null,
            children = emptyList()
        )

        val resourceId = node.viewIdResourceName
        val text = node.text?.toString()
        val contentDescription = node.contentDescription?.toString()
        val className = node.className?.toString()

        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        val boundsObj = Bounds(
            left = bounds.left,
            top = bounds.top,
            right = bounds.right,
            bottom = bounds.bottom
        )

        val children = mutableListOfScreenNode>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                children.add(buildScreenNode(child, depth + 1))
                child.recycle()
            }
        }

        return ScreenNode(
            resourceId = resourceId,
            text = text,
            contentDescription = contentDescription,
            className = className,
            children = children,
            bounds = boundsObj,
            clickable = node.isClickable,
            editable = node.isEditable
        )
    }

    fun getCurrentScreen(): ScreenNode? {
        val rootNode = rootInActiveWindow
        return rootNode?.let {
            val screen = buildScreenNode(it)
            it.recycle()
            screen
        }
    }

    fun getCurrentPackage(): String? = currentPackage

    fun performClick(resourceId: String): Boolean {
        Log.d(TAG, "Attempting to click resourceId: $resourceId")
        val rootNode = rootInActiveWindow ?: return false
        
        val targetNode = findNodeByResourceId(rootNode, resourceId)
        if (targetNode != null) {
            val bounds = android.graphics.Rect()
            targetNode.getBoundsInScreen(bounds)
            val success = clickAt(bounds.centerX(), bounds.centerY())
            targetNode.recycle()
            rootNode.recycle()
            return success
        }
        
        rootNode.recycle()
        return false
    }

    fun performClickByText(text: String): Boolean {
        Log.d(TAG, "Attempting to click by text: $text")
        val rootNode = rootInActiveWindow ?: return false
        
        val targetNode = findNodeByText(rootNode, text)
        if (targetNode != null) {
            val bounds = android.graphics.Rect()
            targetNode.getBoundsInScreen(bounds)
            val success = clickAt(bounds.centerX(), bounds.centerY())
            targetNode.recycle()
            rootNode.recycle()
            return success
        }
        
        rootNode.recycle()
        return false
    }

    private fun findNodeByResourceId(node: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == resourceId && node.isClickable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByResourceId(child, resourceId)
                if (found != null) {
                    return found
                }
                child.recycle()
            }
        }
        return null
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString()
        if (nodeText?.contains(text, ignoreCase = true) == true && node.isClickable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByText(child, text)
                if (found != null) {
                    return found
                }
                child.recycle()
            }
        }
        return null
    }

    fun performType(resourceId: String, text: String): Boolean {
        Log.d(TAG, "Attempting to type '$text' into resourceId: $resourceId")
        val rootNode = rootInActiveWindow ?: return false
        
        val targetNode = findNodeByResourceId(rootNode, resourceId)
        if (targetNode != null) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            Thread.sleep(200)
            
            val arguments = android.os.Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val success = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            targetNode.recycle()
            rootNode.recycle()
            return success
        }
        
        rootNode.recycle()
        return false
    }

    fun performScroll(up: Boolean): Boolean {
        Log.d(TAG, "Performing scroll ${if (up) "up" else "down"}")
        val rootNode = rootInActiveWindow ?: return false
        
        val action = if (up) {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }
        
        val success = rootNode.performAction(action)
        rootNode.recycle()
        return success
    }

    fun pressBack(): Boolean {
        Log.d(TAG, "Pressing back button")
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        Log.d(TAG, "Pressing home button")
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun launchApp(packageName: String): Boolean {
        Log.d(TAG, "Launching app: $packageName")
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "App not found: $packageName", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch app: $packageName", e)
            false
        }
    }

    private fun clickAt(x: Float, y: Float): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            dispatchGesture(gesture, null, null)
        } else {
            false
        }
    }
}

private fun <T> mutableListOf(): MutableList<T> = java.util.ArrayList()
