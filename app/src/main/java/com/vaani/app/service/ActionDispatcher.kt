package com.vaani.app.service

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.vaani.app.data.model.Bounds
import kotlinx.coroutines.delay

class ActionDispatcher(private val service: VaaniAccessibilityService) {

    companion object {
        private const val TAG = "ActionDispatcher"
        private const val ACTION_DELAY = 300L
        private const val SCREEN_UPDATE_DELAY = 500L
        private const val APP_LAUNCH_TIMEOUT = 8000L
        private const val SCREEN_STABILITY_TIMEOUT = 3000L
        private const val SCREEN_STABILITY_POLL = 200L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenReader = ScreenReader(service)
    private var lastScreenHash = 0

    suspend fun click(resourceId: String?, text: String?, bounds: Bounds?): Boolean {
        Log.d(TAG, "Click attempt: resourceId=$resourceId, text=$text")
        
        var result = false
        
        if (resourceId != null) {
            result = clickByResourceId(resourceId)
            if (result) {
                Log.d(TAG, "Click succeeded via resourceId")
                return true
            }
        }
        
        if (text != null && text.isNotBlank()) {
            result = clickByText(text)
            if (result) {
                Log.d(TAG, "Click succeeded via text")
                return true
            }
        }
        
        if (bounds != null) {
            result = clickByCoordinates(bounds)
            if (result) {
                Log.d(TAG, "Click succeeded via coordinates")
                return true
            }
        }
        
        Log.w(TAG, "Click failed: no strategy worked")
        return false
    }

    private fun clickByResourceId(resourceId: String): Boolean {
        val rootNode = service.rootInActiveWindow ?: return false
        
        return try {
            val targetNode = findNodeByResourceId(rootNode, resourceId)
            if (targetNode != null) {
                val clicked = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (!clicked) {
                    clickByCoordinates(getBounds(targetNode))
                } else {
                    true
                }
            } else {
                false
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun clickByText(text: String): Boolean {
        val rootNode = service.rootInActiveWindow ?: return false
        
        return try {
            val targetNode = findNodeByText(rootNode, text)
            if (targetNode != null && targetNode.isClickable) {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                val parent = findParentWithClick(targetNode)
                parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun clickByCoordinates(bounds: Bounds): Boolean {
        return try {
            val centerX = (bounds.left + bounds.right) / 2f
            val centerY = (bounds.top + bounds.bottom) / 2f
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply {
                    moveTo(centerX, centerY)
                }
                val gesture = android.view.accessibility.AccessibilityGestureDescription.Builder()
                    .addStroke(
                        android.view.accessibility.AccessibilityGestureDescription.StrokeDescription(
                            path, 0, 100
                        ), 1
                    )
                    .build()
                service.dispatchGesture(gesture, null, null)
            } else {
                clickPoint(centerX, centerY)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Click by coordinates failed", e)
            false
        }
    }

    private fun clickPoint(x: Float, y: Float): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val gesture = android.view.accessibility.AccessibilityGestureDescription.Builder()
                .addStroke(
                    android.view.accessibility.AccessibilityGestureDescription.StrokeDescription(
                        path, 0, 100
                    ), 1
                )
                .build()
            service.dispatchGesture(gesture, null, null)
        } else {
            false
        }
    }

    suspend fun type(resourceId: String?, text: String): Boolean {
        Log.d(TAG, "Type attempt: resourceId=$resourceId, text=$text")
        
        val targetId = resourceId ?: findEditableFieldId() ?: return false
        
        delay(ACTION_DELAY)
        
        val rootNode = service.rootInActiveWindow ?: return false
        
        return try {
            val targetNode = findNodeByResourceId(rootNode, targetId)
            if (targetNode != null) {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                delay(200)
                
                val args = android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                
                val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                
                if (!result) {
                    clearAndTypeSlow(targetNode, text)
                } else {
                    Log.d(TAG, "Type succeeded via ACTION_SET_TEXT")
                    true
                }
            } else {
                Log.w(TAG, "Type target not found")
                false
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun clearAndTypeSlow(node: AccessibilityNodeInfo, text: String) {
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        
        val arguments = android.os.Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        
        delay(100)
        
        val args = android.os.Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun findEditableFieldId(): String? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        return try {
            findEditableNode(rootNode)?.viewIdResourceName
        } finally {
            rootNode.recycle()
        }
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findEditableNode(child)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    suspend fun scroll(direction: ScrollDirection): Boolean {
        Log.d(TAG, "Scroll: $direction")
        
        delay(ACTION_DELAY)
        
        val rootNode = service.rootInActiveWindow ?: return false
        
        return try {
            val action = when (direction) {
                ScrollDirection.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                ScrollDirection.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            }
            
            val scrolled = rootNode.performAction(action)
            
            if (!scrolled) {
                val scrollNode = findScrollableNode(rootNode)
                scrollNode?.performAction(action) ?: swipeScroll(direction)
            } else {
                true
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun swipeScroll(direction: ScrollDirection) {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        
        val startX = width / 2
        val startY: Int
        val endY: Int
        
        when (direction) {
            ScrollDirection.UP -> {
                startY = (height * 0.7).toInt()
                endY = (height * 0.3).toInt()
            }
            ScrollDirection.DOWN -> {
                startY = (height * 0.3).toInt()
                endY = (height * 0.7).toInt()
            }
            else -> return
        }
        
        swipe(startX, startY, startX, endY, 300)
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findScrollableNode(child)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    suspend fun openApp(packageName: String): Boolean {
        Log.d(TAG, "Opening app: $packageName")
        
        return try {
            val intent = service.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                service.startActivity(intent)
                
                waitForAppLaunch(packageName)
                true
            } else {
                Log.e(TAG, "No launch intent for: $packageName")
                false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "App not found: $packageName", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app: $packageName", e)
            false
        }
    }

    private suspend fun waitForAppLaunch(packageName: String) {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < APP_LAUNCH_TIMEOUT) {
            val currentPackage = service.getCurrentPackage()
            if (currentPackage == packageName) {
                delay(500)
                return
            }
            delay(SCREEN_STABILITY_POLL)
        }
        
        Log.w(TAG, "App launch timeout for: $packageName")
    }

    suspend fun swipe(direction: SwipeDirection, bounds: Bounds? = null) {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        
        val startX: Int
        val startY: Int
        val endX: Int
        val endY: Int
        val duration = 250L
        
        if (bounds != null) {
            startX = (bounds.left + bounds.right) / 2
            startY = (bounds.top + bounds.bottom) / 2
            val range = minOf(bounds.width(), bounds.height()) / 2
            
            when (direction) {
                SwipeDirection.LEFT -> {
                    endX = startX - range
                    endY = startY
                }
                SwipeDirection.RIGHT -> {
                    endX = startX + range
                    endY = startY
                }
                SwipeDirection.UP -> {
                    endX = startX
                    endY = startY - range
                }
                SwipeDirection.DOWN -> {
                    endX = startX
                    endY = startY + range
                }
            }
        } else {
            when (direction) {
                SwipeDirection.LEFT -> {
                    startX = (width * 0.8).toInt()
                    endX = (width * 0.2).toInt()
                    startY = height / 2
                    endY = height / 2
                }
                SwipeDirection.RIGHT -> {
                    startX = (width * 0.2).toInt()
                    endX = (width * 0.8).toInt()
                    startY = height / 2
                    endY = height / 2
                }
                SwipeDirection.UP -> {
                    startX = width / 2
                    endX = width / 2
                    startY = (height * 0.7).toInt()
                    endY = (height * 0.3).toInt()
                }
                SwipeDirection.DOWN -> {
                    startX = width / 2
                    endX = width / 2
                    startY = (height * 0.3).toInt()
                    endY = (height * 0.7).toInt()
                }
            }
        }
        
        swipe(startX, startY, endX, endY, duration)
    }

    private fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val path = Path().apply {
                    moveTo(x1.toFloat(), y1.toFloat())
                    lineTo(x2.toFloat(), y2.toFloat())
                }
                val gesture = android.view.accessibility.AccessibilityGestureDescription.Builder()
                    .addStroke(
                        android.view.accessibility.AccessibilityGestureDescription.StrokeDescription(
                            path, 0, duration
                        ), 1
                    )
                    .build()
                service.dispatchGesture(gesture, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Swipe gesture failed", e)
            }
        }
    }

    suspend fun verify(expectedText: String): VerifyResult {
        delay(SCREEN_UPDATE_DELAY)
        
        val capture = screenReader.captureScreen()
        val screenText = capture.screenText.lowercase()
        val searchText = expectedText.lowercase()
        
        return when {
            screenText.contains(searchText) -> VerifyResult.FOUND
            screenText.split("|").any { it.trim().contains(searchText, ignoreCase = true) } -> VerifyResult.PARTIAL_MATCH
            else -> VerifyResult.NOT_FOUND
        }
    }

    suspend fun waitForScreen(ms: Long) {
        val startTime = System.currentTimeMillis()
        var stableSince = startTime
        var lastHash = captureScreenHash()
        
        while (System.currentTimeMillis() - startTime < ms) {
            delay(SCREEN_STABILITY_POLL)
            
            val currentHash = captureScreenHash()
            if (currentHash != lastHash) {
                stableSince = System.currentTimeMillis()
                lastHash = currentHash
            }
            
            if (System.currentTimeMillis() - stableSince > 500) {
                Log.d(TAG, "Screen stable after ${System.currentTimeMillis() - startTime}ms")
                return
            }
        }
        
        Log.d(TAG, "Wait timeout after $ms")
    }

    private fun captureScreenHash(): Int {
        val rootNode = service.rootInActiveWindow ?: return 0
        return try {
            rootNode.toString().hashCode()
        } finally {
            rootNode.recycle()
        }
    }

    fun pressBack(): Boolean {
        Log.d(TAG, "Pressing back")
        return service.performGlobalAction(android.view.accessibility.AccessibilityService.GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        Log.d(TAG, "Pressing home")
        return service.performGlobalAction(android.view.accessibility.AccessibilityService.GLOBAL_ACTION_HOME)
    }

    fun pressRecents(): Boolean {
        return service.performGlobalAction(android.view.accessibility.AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    fun openNotifications(): Boolean {
        return service.performGlobalAction(android.view.accessibility.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun openQuickSettings(): Boolean {
        return service.performGlobalAction(android.view.accessibility.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }

    private fun findNodeByResourceId(node: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == resourceId) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByResourceId(child, resourceId)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString()
        if (nodeText?.contains(text, ignoreCase = true) == true) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByText(child, text)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findParentWithClick(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isClickable && node.parent != null) {
            return node.parent
        }
        
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) return parent
            parent = parent.parent
        }
        return null
    }

    private fun getBounds(node: AccessibilityNodeInfo): Bounds {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        return Bounds(rect.left, rect.top, rect.right, rect.bottom)
    }
}
