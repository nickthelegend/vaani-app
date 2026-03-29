package com.vaani.app.utils

import com.vaani.app.data.models.AppLanguage

object ResponseTemplates {

    fun taskStarted(language: AppLanguage, task: String): String = when (language) {
        AppLanguage.TELUGU -> "$task మొదలుపెడుతున్నాను..."
        AppLanguage.HINDI -> "$task शुरू कर रहा हूं..."
        AppLanguage.TAMIL -> "$task தொடங்குகிறேன்..."
        AppLanguage.KANNADA -> "$task ಪ್ರಾರಂಭಿಸುತ್ತಿದ್ದೇನೆ..."
        AppLanguage.ENGLISH -> "Starting $task..."
    }

    fun taskDone(language: AppLanguage): String = when (language) {
        AppLanguage.TELUGU -> "పని పూర్తయింది!"
        AppLanguage.HINDI -> "काम हो गया!"
        AppLanguage.TAMIL -> "முடிந்தது!"
        AppLanguage.KANNADA -> "ಮುಗಿದಿದೆ!"
        AppLanguage.ENGLISH -> "Done!"
    }

    fun taskFailed(language: AppLanguage, reason: String): String = when (language) {
        AppLanguage.TELUGU -> "క్షమించండి, పని అవలేదు"
        AppLanguage.HINDI -> "माफ़ करें, नहीं हो पाया"
        AppLanguage.TAMIL -> "மன்னிக்கவும், முடியவில்லை"
        AppLanguage.KANNADA -> "ಕ್ಷಮಿಸಿ, ಆಗಲಿಲ್ಲ"
        AppLanguage.ENGLISH -> "Sorry, couldn't complete that"
    }

    fun listening(language: AppLanguage): String = when (language) {
        AppLanguage.TELUGU -> "చెప్పండి..."
        AppLanguage.HINDI -> "बोलिए..."
        AppLanguage.TAMIL -> "சொல்லுங்கள்..."
        AppLanguage.KANNADA -> "ಹೇಳಿ..."
        AppLanguage.ENGLISH -> "Listening..."
    }
}
