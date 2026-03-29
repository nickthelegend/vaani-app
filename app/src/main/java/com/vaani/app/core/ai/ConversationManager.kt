package com.vaani.app.core.ai

import com.vaani.app.data.models.ParsedIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationManager @Inject constructor() {
    private var lastIntent: ParsedIntent? = null
    private val history = mutableListOf<String>()

    fun recordCommand(command: String) {
        history.add(command)
    }

    fun setLastIntent(intent: ParsedIntent) {
        lastIntent = intent
    }

    fun getLastIntent(): ParsedIntent? = lastIntent
}
