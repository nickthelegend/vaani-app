package com.vaani.app.utils

import androidx.lifecycle.viewModelScope
import com.vaani.app.core.pipeline.TaskPipeline
import com.vaani.app.data.models.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoMode @Inject constructor(
    private val taskPipeline: TaskPipeline
) {
    suspend fun runDemoSequence(language: AppLanguage) {
        // Task 1: YouTube Search
        taskPipeline.executeTask(
            if (language == AppLanguage.TELUGU) "YouTube లో AR Rahman పాటలు ప్లే చెయ్యి" else "Play AR Rahman hits on YouTube", 
            language
        )
        delay(3000)

        // Task 2: Chrome Search
        taskPipeline.executeTask(
            if (language == AppLanguage.TELUGU) "Chrome లో VibeCon 2025 గురించి వెతుకు" else "Search for VibeCon 2025 on Chrome", 
            language
        )
        delay(3000)

        // Task 3: WhatsApp Search (Safe Demo)
        taskPipeline.executeTask(
            if (language == AppLanguage.TELUGU) "WhatsApp లో సెర్చ్ ఓపెన్ చెయ్యి" else "Open search in WhatsApp", 
            language
        )
    }
}
