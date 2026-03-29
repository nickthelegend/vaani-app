package com.vaani.app.core.accessibility

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.vaani.app.data.models.ActionType
import com.vaani.app.data.models.AgentAction
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface ActionDispatcher {
    suspend fun performClick(resourceId: String?, text: String?): Boolean
    suspend fun performLongClick(resourceId: String?, text: String?): Boolean
    suspend fun performType(resourceId: String?, inputText: String): Boolean
    suspend fun performClearAndType(resourceId: String?, inputText: String): Boolean
    suspend fun performScroll(resourceId: String?, up: Boolean): Boolean
    suspend fun verifyScreen(expectedText: String): Boolean
}

class ActionDispatcherImpl @Inject constructor(
    private val smartElementFinder: SmartElementFinder
) : ActionDispatcher {

    private val TAG = "ActionDispatcher"

    override suspend fun performClick(resourceId: String?, text: String?): Boolean {
        val node = smartElementFinder.findNode(resourceId, text) ?: return false
        val clickable = findClickableParent(node) ?: node
        return clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    override suspend fun performLongClick(resourceId: String?, text: String?): Boolean {
        val node = smartElementFinder.findNode(resourceId, text) ?: return false
        val clickable = findClickableParent(node) ?: node
        return clickable.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    override suspend fun performType(resourceId: String?, inputText: String): Boolean {
        val node = smartElementFinder.findNode(resourceId, null, isEditable = true) ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, inputText)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    override suspend fun performClearAndType(resourceId: String?, inputText: String): Boolean {
        val node = smartElementFinder.findNode(resourceId, null, isEditable = true) ?: return false
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "") })
        delay(200)
        return performType(resourceId, inputText)
    }

    override suspend fun performScroll(resourceId: String?, up: Boolean): Boolean {
        val node = smartElementFinder.findNode(resourceId, null, isScrollable = true) ?: return false
        return node.performAction(if (up) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    override suspend fun verifyScreen(expectedText: String): Boolean {
        val node = smartElementFinder.findNode(null, expectedText)
        return node != null
    }

    private fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent ?: break
        }
        return null
    }
}
