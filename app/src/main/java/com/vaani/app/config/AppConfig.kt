package com.vaani.app.config

object AppConfig {
    const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    const val GEMINI_MODEL = "gemini-1.5-flash"
    const val MAX_TOKENS = 1024
    const val TEMPERATURE = 0.1f
    
    const val SCREEN_TREE_MAX_DEPTH = 15
    const val ACTION_DELAY_MS = 500L
    const val SCREEN_UPDATE_DELAY_MS = 800L
    const val MAX_RETRIES = 2
}
