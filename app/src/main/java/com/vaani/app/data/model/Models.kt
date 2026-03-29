package com.vaani.app.data.model

data class Task(
    val id: String,
    val description: String,
    val appName: String,
    val timestamp: Long,
    val status: TaskStatus,
    val language: String
)

enum class TaskStatus {
    DONE,
    FAILED,
    IN_PROGRESS
}

enum class AppLanguage(val displayName: String, val code: String, val nativeName: String) {
    HINDI("हिंदी", "hi-IN", "Hindi"),
    TELUGU("తెలుగు", "te-IN", "Telugu"),
    TAMIL("தமிழ்", "ta-IN", "Tamil"),
    KANNADA("ಕನ್ನಡ", "kn-IN", "Kannada"),
    ENGLISH("English", "en-IN", "English")
}

enum class AgentState {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS
}
