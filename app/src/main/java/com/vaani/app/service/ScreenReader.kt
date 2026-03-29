package com.vaani.app.service

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.vaani.app.data.model.Bounds
import com.vaani.app.data.model.ScreenNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScreenReader(private val service: VaaniAccessibilityService) {

    companion object {
        private const val TAG = "ScreenReader"
        private const val MAX_DEPTH = 20
        private const val MAX_CHILDREN = 15
    }

    fun captureScreen(): ScreenCapture {
        val rootNode = service.rootInActiveWindow ?: return emptyCapture()
        
        return try {
            val packageName = service.getCurrentPackage() ?: "unknown"
            val nodeTree = buildScreenNode(rootNode, 0)
            val screenText = extractAllText(rootNode)
            val interactableElements = extractInteractableElements(rootNode, 0)

            ScreenCapture(
                packageName = packageName,
                activityName = "",
                timestamp = System.currentTimeMillis(),
                nodeTree = nodeTreeToJson(nodeTree),
                screenText = screenText,
                interactableElements = interactableElements
            )
        } finally {
            rootNode.recycle()
        }
    }

    fun captureScreenFiltered(): ScreenCapture {
        val capture = captureScreen()
        
        val filteredElements = capture.interactableElements.filter { element ->
            element.resourceId != null || 
            element.text?.isNotBlank() == true || 
            element.contentDesc?.isNotBlank() == true
        }
        
        return capture.copy(
            interactableElements = filteredElements
        )
    }

    fun watchForScreenChange(onChanged: (ScreenCapture) -> Unit) {
        service.addScreenChangeListener { capture ->
            onChanged(capture)
        }
    }

    private fun emptyCapture(): ScreenCapture {
        return ScreenCapture(
            packageName = "",
            activityName = "",
            timestamp = System.currentTimeMillis(),
            nodeTree = "{}",
            screenText = "",
            interactableElements = emptyList()
        )
    }

    private fun buildScreenNode(node: AccessibilityNodeInfo, depth: Int): ScreenNode {
        if (depth > MAX_DEPTH) return ScreenNode(
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

        val children = mutableListOf<ScreenNode>()
        val childCount = node.childCount.coerceAtMost(MAX_CHILDREN)
        
        for (i in 0 until childCount) {
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

    private fun extractAllText(node: AccessibilityNodeInfo, depth: Int = 0): String {
        if (depth > MAX_DEPTH) return ""
        
        val texts = mutableListOf<String>()
        
        node.text?.toString()?.let { if (it.isNotBlank()) texts.add(it) }
        node.contentDescription?.toString()?.let { if (it.isNotBlank()) texts.add(it) }
        
        for (i in 0 until node.childCount.coerceAtMost(MAX_CHILDREN)) {
            val child = node.getChild(i)
            if (child != null) {
                texts.add(extractAllText(child, depth + 1))
                child.recycle()
            }
        }
        
        return texts.filter { it.isNotBlank() }.joinToString(" | ")
    }

    private fun extractInteractableElements(node: AccessibilityNodeInfo, depth: Int): List<UIElement> {
        if (depth > MAX_DEPTH) return emptyList()
        
        val elements = mutableListOf<UIElement>()
        
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        val isInteresting = node.isClickable || 
                           node.isEditable || 
                           node.isScrollable || 
                           node.viewIdResourceName != null ||
                           node.text?.isNotBlank() == true
        
        if (isInteresting && bounds.width() > 0 && bounds.height() > 0) {
            elements.add(
                UIElement(
                    resourceId = node.viewIdResourceName,
                    text = node.text?.toString(),
                    contentDesc = node.contentDescription?.toString(),
                    className = node.className?.toString() ?: "Unknown",
                    bounds = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom),
                    isClickable = node.isClickable,
                    isEditable = node.isEditable,
                    isScrollable = node.isScrollable,
                    depth = depth
                )
            )
        }
        
        for (i in 0 until node.childCount.coerceAtMost(MAX_CHILDREN)) {
            val child = node.getChild(i)
            if (child != null) {
                elements.addAll(extractInteractableElements(child, depth + 1))
                child.recycle()
            }
        }
        
        return elements
    }

    private fun nodeTreeToJson(node: ScreenNode, depth: Int = 0): String {
        if (depth > 10) return "{}"
        
        val sb = StringBuilder()
        sb.append("{")
        
        node.resourceId?.let { sb.append("\"resourceId\":\"$it\",") }
        node.text?.let { sb.append("\"text\":\"${it.replace("\"", "\\\"")}\",") }
        node.contentDescription?.let { sb.append("\"contentDesc\":\"${it.replace("\"", "\\\"")}\",") }
        node.className?.let { sb.append("\"className\":\"$it\",") }
        sb.append("\"clickable\":${node.clickable},")
        sb.append("\"editable\":${node.editable}")
        
        if (node.children.isNotEmpty()) {
            sb.append(",\"children\":[")
            sb.append(node.children.take(5).joinToString(",") { nodeTreeToJson(it, depth + 1) })
            sb.append("]")
        }
        
        sb.append("}")
        return sb.toString()
    }

    data class ScreenCapture(
        val packageName: String,
        val activityName: String,
        val timestamp: Long,
        val nodeTree: String,
        val screenText: String,
        val interactableElements: List<UIElement>
    )

    data class UIElement(
        val resourceId: String?,
        val text: String?,
        val contentDesc: String?,
        val className: String,
        val bounds: Rect,
        val isClickable: Boolean,
        val isEditable: Boolean,
        val isScrollable: Boolean,
        val depth: Int
    )
}

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}

enum class VerifyResult {
    FOUND,
    NOT_FOUND,
    PARTIAL_MATCH
}
