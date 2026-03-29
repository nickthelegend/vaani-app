package com.vaani.app.data.models

enum class AppLanguage(val displayName: String, val nativeScript: String, val code: String) {
    TELUGU("Telugu", "తెలుగు", "te-IN"),
    HINDI("Hindi", "हिंदी", "hi-IN"),
    TAMIL("Tamil", "தமிழ்", "ta-IN"),
    KANNADA("Kannada", "ಕನ್ನಡ", "kn-IN"),
    ENGLISH("English", "English", "en-IN")
}
