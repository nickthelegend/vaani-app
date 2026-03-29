package com.vaani.app.core.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartElementFinder @Inject constructor() {
    
    fun findNode(
        resourceId: String? = null,
        textAt: String? = null,
        isVisible: Boolean = true,
        isClickable: Boolean = false,
        isEditable: Boolean = false,
        isScrollable: Boolean = false
    ): AccessibilityNodeInfo? {
        val root = VaaniAccessibilityService.getService()?.rootInActiveWindow ?: return null
        return findRecursive(root, resourceId, textAt, isVisible, isClickable, isEditable, isScrollable)
    }

    private fun findRecursive(
        node: AccessibilityNodeInfo,
        resourceId: String?,
        textAt: String?,
        isVisible: Boolean,
        isClickable: Boolean,
        isEditable: Boolean,
        isScrollable: Boolean
    ): AccessibilityNodeInfo? {
        if (isVisible && !node.isVisibleToUser) return null
        
        var matches = true
        if (resourceId != null && node.viewIdResourceName != resourceId) matches = false
        if (textAt != null && !node.text?.toString()?.contains(textAt, ignoreCase = true)!! && 
            !node.contentDescription?.toString()?.contains(textAt, ignoreCase = true)!!) matches = false
        
        if (isClickable && !node.isClickable) matches = false
        if (isEditable && !node.isEditable) matches = false
        if (isScrollable && !node.isScrollable) matches = false

        if (matches) return node

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findRecursive(child, resourceId, textAt, isVisible, isClickable, isEditable, isScrollable)
            if (found != null) return found
        }
        
        return null
    }
}
