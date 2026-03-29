package com.vaani.app.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.vaani.app.data.models.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    private val TAG = "TTSManager"
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingSpeech: String? = null
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            setLanguage(currentLanguage)
            pendingSpeech?.let {
                speak(it, currentLanguage)
                pendingSpeech = null
            }
        } else {
            Log.e(TAG, "Initialization failed")
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        if (!isReady) return
        
        val locale = Locale.forLanguageTag(language.code)
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language not supported: ${language.code}")
        }
        
        tts?.setPitch(1.1f)
        tts?.setSpeechRate(0.95f)
    }

    fun speak(text: String, language: AppLanguage? = null) {
        language?.let { setLanguage(it) }
        
        if (!isReady) {
            pendingSpeech = text
            return
        }
        
        Log.d(TAG, "Speaking: $text")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VaaniTTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
