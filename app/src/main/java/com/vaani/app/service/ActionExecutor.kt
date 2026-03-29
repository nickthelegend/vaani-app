package com.vaani.app.service

import android.util.Log
import com.vaani.app.data.model.AgentAction
import com.vaani.app.data.model.ExecutionResult
import com.vaani.app.data.model.Bounds
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor() {

    companion object {
        private const val TAG = "ActionExecutor"
    }

    private var accessibilityService: VaaniAccessibilityService? = null
    private var actionDispatcher: ActionDispatcher? = null
    private var smartElementFinder: SmartElementFinder? = null

    fun setAccessibilityService(service: VaaniAccessibilityService) {
        this.accessibilityService = service
        this.actionDispatcher = ActionDispatcher(service)
        this.smartElementFinder = SmartElementFinder(service)
    }

    suspend fun execute(actions: List<AgentAction>): ExecutionResult {
        val service = accessibilityService
        val dispatcher = actionDispatcher
        val finder = smartElementFinder
        
        if (service == null || dispatcher == null || finder == null) {
            return ExecutionResult(
                success = false,
                completedActions = 0,
                totalActions = actions.size,
                errorMessage = "Accessibility service not available"
            )
        }

        var completedCount = 0
        var lastError: String? = null

        Log.d(TAG, "Starting execution of ${actions.size} actions")

        for ((index, action) in actions.withIndex()) {
            try {
                Log.d(TAG, "Executing action ${index + 1}/${actions.size}: ${action.type} - ${action.description}")
                
                delay(300)
                
                val success = when (action.type) {
                    ActionType.CLICK -> {
                        val bounds = action.resourceId?.let { resId ->
                            finder.findElement(resourceId = resId)?.bounds?.let { rect ->
                                Bounds(rect.left, rect.top, rect.right, rect.bottom)
                            }
                        }
                        dispatcher.click(action.resourceId, action.text, bounds)
                    }
                    
                    ActionType.TYPE -> {
                        dispatcher.type(action.resourceId, action.text ?: "")
                    }
                    
                    ActionType.SCROLL_UP -> {
                        dispatcher.scroll(ScrollDirection.UP)
                    }
                    
                    ActionType.SCROLL_DOWN -> {
                        dispatcher.scroll(ScrollDirection.DOWN)
                    }
                    
                    ActionType.BACK -> {
                        dispatcher.pressBack()
                    }
                    
                    ActionType.HOME -> {
                        dispatcher.pressHome()
                    }
                    
                    ActionType.LAUNCH_APP -> {
                        action.appPackage?.let { dispatcher.openApp(it) } ?: false
                    }
                    
                    ActionType.WAIT -> {
                        val waitTime = action.text?.toLongOrNull() ?: 1000L
                        dispatcher.waitForScreen(waitTime)
                        true
                    }
                }

                if (success) {
                    completedCount++
                    Log.d(TAG, "Action ${index + 1} succeeded")
                    
                    if (action.type != ActionType.WAIT && 
                        action.type != ActionType.HOME && 
                        action.type != ActionType.BACK) {
                        delay(500)
                    }
                } else {
                    lastError = "Failed: ${action.type}"
                    Log.w(TAG, "Action ${index + 1} failed: $lastError")
                }
                
            } catch (e: Exception) {
                lastError = "Error: ${action.type}: ${e.message}"
                Log.e(TAG, "Action ${index + 1} exception", e)
            }
        }

        val finalSuccess = completedCount == actions.size
        Log.d(TAG, "Execution complete: $completedCount/${actions.size} actions successful")
        
        return ExecutionResult(
            success = finalSuccess,
            completedActions = completedCount,
            totalActions = actions.size,
            errorMessage = lastError
        )
    }

    suspend fun executeSingle(action: AgentAction): Boolean {
        val dispatcher = actionDispatcher ?: return false
        
        return when (action.type) {
            ActionType.CLICK -> {
                val bounds = action.resourceId?.let { resId ->
                    smartElementFinder?.findElement(resourceId = resId)?.bounds?.let { rect ->
                        Bounds(rect.left, rect.top, rect.right, rect.bottom)
                    }
                }
                dispatcher.click(action.resourceId, action.text, bounds)
            }
            
            ActionType.TYPE -> {
                dispatcher.type(action.resourceId, action.text ?: "")
            }
            
            ActionType.SCROLL_UP -> dispatcher.scroll(ScrollDirection.UP)
            ActionType.SCROLL_DOWN -> dispatcher.scroll(ScrollDirection.DOWN)
            ActionType.BACK -> dispatcher.pressBack()
            ActionType.HOME -> dispatcher.pressHome()
            
            ActionType.LAUNCH_APP -> {
                action.appPackage?.let { dispatcher.openApp(it) } ?: false
            }
            
            ActionType.WAIT -> {
                val waitTime = action.text?.toLongOrNull() ?: 1000L
                dispatcher.waitForScreen(waitTime)
                true
            }
        }
    }

    fun verifyScreen(expectedText: String): VerifyResult {
        val dispatcher = actionDispatcher ?: return VerifyResult.PARTIAL_MATCH
        
        return kotlinx.coroutines.runBlocking {
            dispatcher.verify(expectedText)
        }
    }
}
