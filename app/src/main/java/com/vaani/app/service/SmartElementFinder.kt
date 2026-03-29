package com.vaani.app.service

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class SmartElementFinder(private val service: VaaniAccessibilityService) {

    companion object {
        private const val TAG = "SmartElementFinder"
        
        private val BUTTON_CLASSES = setOf(
            "android.widget.Button",
            "android.widget.TextView",
            "android.widget.ImageButton",
            "android.widget.CheckBox",
            "android.widget.RadioButton",
            "android.widget.ToggleButton"
        )
        
        private val EDIT_CLASSES = setOf(
            "android.widget.EditText",
            "android.widget.TextView"
        )
        
        private val SCROLL_CLASSES = setOf(
            "android.widget.ScrollView",
            "androidx.recyclerview.widget.RecyclerView",
            "android.widget.ListView",
            "android.widget.HorizontalScrollView",
            "androidx.viewpager.widget.ViewPager"
        )
    }

    data class ElementMatch(
        val node: AccessibilityNodeInfo?,
        val strategy: FindStrategy,
        val confidence: Float,
        val bounds: Rect
    )

    enum class FindStrategy {
        RESOURCE_ID,
        TEXT_EXACT,
        TEXT_CONTAINS,
        CONTENT_DESC,
        CLASS_NAME,
        POSITION,
        FALLBACK
    }

    fun findElement(
        resourceId: String? = null,
        text: String? = null,
        contentDesc: String? = null,
        className: String? = null,
        bounds: Rect? = null,
        matchType: MatchType = MatchType.CLICKABLE
    ): ElementMatch? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        return try {
            if (resourceId != null) {
                val match = findByResourceId(rootNode, resourceId, matchType)
                if (match != null) {
                    return ElementMatch(match, FindStrategy.RESOURCE_ID, 1.0f, getBounds(match))
                }
            }
            
            if (text != null && text.isNotBlank()) {
                val match = findByText(rootNode, text, exact = true, matchType = matchType)
                if (match != null) {
                    return ElementMatch(match, FindStrategy.TEXT_EXACT, 0.9f, getBounds(match))
                }
                
                val partialMatch = findByText(rootNode, text, exact = false, matchType = matchType)
                if (partialMatch != null) {
                    return ElementMatch(partialMatch, FindStrategy.TEXT_CONTAINS, 0.7f, getBounds(partialMatch))
                }
            }
            
            if (contentDesc != null && contentDesc.isNotBlank()) {
                val match = findByContentDesc(rootNode, contentDesc, matchType)
                if (match != null) {
                    return ElementMatch(match, FindStrategy.CONTENT_DESC, 0.85f, getBounds(match))
                }
            }
            
            if (bounds != null) {
                val match = findByPosition(rootNode, bounds, matchType)
                if (match != null) {
                    return ElementMatch(match, FindStrategy.POSITION, 0.6f, getBounds(match))
                }
            }
            
            val fallbackMatch = findFallback(rootNode, matchType)
            if (fallbackMatch != null) {
                return ElementMatch(fallbackMatch, FindStrategy.FALLBACK, 0.3f, getBounds(fallbackMatch))
            }
            
            null
        } finally {
            rootNode.recycle()
        }
    }

    fun findAllElements(
        resourceId: String? = null,
        text: String? = null,
        contentDesc: String? = null,
        matchType: MatchType = MatchType.CLICKABLE
    ): List<ElementMatch> {
        val rootNode = service.rootInActiveWindow ?: return emptyList()
        
        return try {
            val elements = mutableListOf<ElementMatch>()
            
            findAllMatching(rootNode, resourceId, text, contentDesc, matchType, elements)
            
            elements
        } finally {
            rootNode.recycle()
        }
    }

    private fun findAllMatching(
        node: AccessibilityNodeInfo,
        resourceId: String?,
        text: String?,
        contentDesc: String?,
        matchType: MatchType,
        results: MutableList<ElementMatch>
    ) {
        if (matches(node, resourceId, text, contentDesc, matchType)) {
            results.add(ElementMatch(node, FindStrategy.RESOURCE_ID, 1.0f, getBounds(node)))
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                findAllMatching(child, resourceId, text, contentDesc, matchType, results)
                child.recycle()
            }
        }
    }

    private fun findByResourceId(node: AccessibilityNodeInfo, resourceId: String, matchType: MatchType): AccessibilityNodeInfo? {
        if (matchesByResourceId(node, resourceId, matchType)) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findByResourceId(child, resourceId, matchType)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findByText(node: AccessibilityNodeInfo, text: String, exact: Boolean, matchType: MatchType): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: ""
        
        val matches = if (exact) {
            nodeText.equals(text, ignoreCase = true)
        } else {
            nodeText.contains(text, ignoreCase = true)
        }
        
        if (matches && matchesType(node, matchType)) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findByText(child, text, exact, matchType)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findByContentDesc(node: AccessibilityNodeInfo, contentDesc: String, matchType: MatchType): AccessibilityNodeInfo? {
        val nodeDesc = node.contentDescription?.toString() ?: ""
        
        if (nodeDesc.contains(contentDesc, ignoreCase = true) && matchesType(node, matchType)) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findByContentDesc(child, contentDesc, matchType)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findByPosition(node: AccessibilityNodeInfo, bounds: Rect, matchType: MatchType): AccessibilityNodeInfo? {
        val nodeBounds = android.graphics.Rect()
        node.getBoundsInScreen(nodeBounds)
        
        val centerX = nodeBounds.centerX()
        val centerY = nodeBounds.centerY()
        
        if (bounds.contains(centerX, centerY) && matchesType(node, matchType)) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findByPosition(child, bounds, matchType)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun findFallback(node: AccessibilityNodeInfo, matchType: MatchType): AccessibilityNodeInfo? {
        when (matchType) {
            MatchType.CLICKABLE -> {
                if (node.isClickable && isImportantNode(node)) return node
            }
            MatchType.EDITABLE -> {
                if (node.isEditable) return node
            }
            MatchType.SCROLLABLE -> {
                if (node.isScrollable) return node
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findFallback(child, matchType)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    private fun matches(
        node: AccessibilityNodeInfo,
        resourceId: String?,
        text: String?,
        contentDesc: String?,
        matchType: MatchType
    ): Boolean {
        if (!matchesType(node, matchType)) return false
        
        resourceId?.let {
            if (node.viewIdResourceName == it) return true
        }
        
        text?.let {
            val nodeText = node.text?.toString() ?: ""
            if (nodeText.contains(it, ignoreCase = true)) return true
        }
        
        contentDesc?.let {
            val nodeDesc = node.contentDescription?.toString() ?: ""
            if (nodeDesc.contains(it, ignoreCase = true)) return true
        }
        
        return false
    }

    private fun matchesByResourceId(node: AccessibilityNodeInfo, resourceId: String, matchType: MatchType): Boolean {
        if (node.viewIdResourceName != resourceId) return false
        return matchesType(node, matchType)
    }

    private fun matchesType(node: AccessibilityNodeInfo, matchType: MatchType): Boolean {
        return when (matchType) {
            MatchType.ANY -> true
            MatchType.CLICKABLE -> node.isClickable || isButtonLike(node)
            MatchType.EDITABLE -> node.isEditable || isEditLike(node)
            MatchType.SCROLLABLE -> node.isScrollable || isScrollLike(node)
        }
    }

    private fun isButtonLike(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return BUTTON_CLASSES.any { className.contains(it) }
    }

    private fun isEditLike(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return EDIT_CLASSES.any { className.contains(it) }
    }

    private fun isScrollLike(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return SCROLL_CLASSES.any { className.contains(it) }
    }

    private fun isImportantNode(node: AccessibilityNodeInfo): Boolean {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        if (bounds.width() < 20 || bounds.height() < 20) return false
        if (bounds.width() > 2000 || bounds.height() > 2000) return false
        
        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        
        if (text.isNotBlank() || contentDesc.isNotBlank()) return true
        if (node.viewIdResourceName != null) return true
        
        return false
    }

    private fun getBounds(node: AccessibilityNodeInfo): Rect {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        return bounds
    }

    fun findClickableElementNear(text: String): AccessibilityNodeInfo? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        return try {
            findClickableContainingText(rootNode, text)
        } finally {
            rootNode.recycle()
        }
    }

    private fun findClickableContainingText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        
        if (nodeText.contains(text, ignoreCase = true)) {
            if (node.isClickable) return node
            
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) return parent
                parent = parent.parent
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findClickableContainingText(child, text)
                if (found != null) return found
                child.recycle()
            }
        }
        return null
    }

    fun findScrollContainer(): AccessibilityNodeInfo? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        return try {
            findScrollableNode(rootNode)
        } finally {
            rootNode.recycle()
        }
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable || isScrollLike(node)) return node
        
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

    enum class MatchType {
        ANY,
        CLICKABLE,
        EDITABLE,
        SCROLLABLE
    }
}
