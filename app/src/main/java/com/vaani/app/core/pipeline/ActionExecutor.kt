package com.vaani.app.core.pipeline

import android.util.Log
import com.vaani.app.core.accessibility.ActionDispatcher
import com.vaani.app.core.accessibility.VaaniAccessibilityService
import com.vaani.app.data.models.ActionType
import com.vaani.app.data.models.AgentAction
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    private val actionDispatcher: ActionDispatcher
) {
    private val TAG = "ActionExecutor"

    suspend fun executeWithRetry(action: AgentAction, maxRetries: Int): Boolean {
        repeat(maxRetries) { attempt ->
            val result = executeAction(action)
            if (result) return true
            Log.w(TAG, "Action failed, retrying... ($attempt/$maxRetries)")
            delay(500 * (attempt + 1L))
        }
        return false
    }

    private suspend fun executeAction(action: AgentAction): Boolean {
        val service = VaaniAccessibilityService.getService() ?: return false
        
        return when (action.type) {
            ActionType.OPEN_APP -> service.launchApp(action.text ?: "")
            ActionType.CLICK -> actionDispatcher.performClick(action.resourceId, action.text)
            ActionType.LONG_CLICK -> actionDispatcher.performLongClick(action.resourceId, action.text)
            ActionType.TYPE -> actionDispatcher.performType(action.resourceId, action.text ?: "")
            ActionType.CLEAR_TYPE -> actionDispatcher.performClearAndType(action.resourceId, action.text ?: "")
            ActionType.SCROLL_UP -> actionDispatcher.performScroll(action.resourceId, up = true)
            ActionType.SCROLL_DOWN -> actionDispatcher.performScroll(action.resourceId, up = false)
            ActionType.BACK -> service.pressBack()
            ActionType.HOME -> service.pressHome()
            ActionType.WAIT -> {
                delay(action.text?.toLongOrNull() ?: 1000)
                true
            }
            ActionType.VERIFY -> actionDispatcher.verifyScreen(action.text ?: "")
            else -> false
        }
    }
}
