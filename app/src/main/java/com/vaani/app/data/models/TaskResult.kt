package com.vaani.app.data.models

data class TaskResult(
    val success: Boolean,
    val summary: String,
    val actionsCount: Int,
    val errorMessage: String? = null
)

enum class AgentState { IDLE, LISTENING, PROCESSING, SUCCESS, FAILED }
