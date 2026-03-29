package com.vaani.app.core.pipeline

import android.util.Log
import com.vaani.app.core.accessibility.VaaniAccessibilityService
import com.vaani.app.core.ai.GeminiClient
import com.vaani.app.core.voice.TTSManager
import com.vaani.app.data.db.TaskEntity
import com.vaani.app.data.models.*
import com.vaani.app.data.repository.TaskRepository
import com.vaani.app.utils.ResponseTemplates
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskPipeline @Inject constructor(
    private val geminiClient: GeminiClient,
    private val actionExecutor: ActionExecutor,
    private val ttsManager: TTSManager,
    private val taskRepository: TaskRepository
) {
    private val TAG = "TaskPipeline"

    suspend fun executeTask(voiceInput: String, language: AppLanguage): TaskResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Intent Parsing
            val intent = geminiClient.parseIntent(voiceInput, language)
            ttsManager.speak(ResponseTemplates.taskStarted(language, intent.translatedEnglish))
            
            // 2. Open Target App
            val service = VaaniAccessibilityService.getService()
            if (intent.packageName != null && intent.packageName != VaaniAccessibilityService.currentPackage.value) {
                service?.launchApp(intent.packageName)
                delay(2000) // Initial wait
            }

            // 3. Action Planning & Execution Loop
            // For simplicity in this demo, we'll do one big sequence
            val screenTree = service?.getScreenTreeJson() ?: "{}"
            val actions = geminiClient.getActionPlan(screenTree, intent)
            
            var allSuccess = true
            for (action in actions) {
                val success = actionExecutor.executeWithRetry(action, maxRetries = 2)
                if (!success && action.type != ActionType.VERIFY) {
                    allSuccess = false
                    // Error recovery would happen here
                }
                delay(800)
            }

            // 4. Record Result
            val result = TaskResult(allSuccess, intent.translatedEnglish, actions.size)
            
            val taskEntity = TaskEntity(
                description = voiceInput,
                translatedDescription = intent.translatedEnglish,
                appPackage = intent.packageName,
                appName = intent.app,
                status = if (allSuccess) "DONE" else "FAILED",
                language = language.code,
                durationMs = System.currentTimeMillis() - startTime
            )
            taskRepository.insertTask(taskEntity)
            
            ttsManager.speak(
                if (allSuccess) ResponseTemplates.taskDone(language)
                else ResponseTemplates.taskFailed(language, "could not complete")
            )

            return result
        } catch (e: Exception) {
            Log.e(TAG, "Task execution failed", e)
            ttsManager.speak(ResponseTemplates.taskFailed(language, e.message ?: "error"))
            return TaskResult(false, "Error", 0, e.message)
        }
    }
}
