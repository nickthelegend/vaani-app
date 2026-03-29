package com.vaani.app.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class WakeWordDetector(private val context: Context) {

    companion object {
        private const val TAG = "WakeWordDetector"
        
        private val WAKE_WORDS = listOf(
            "वाणी",    // Hindi - Vaani
            "वानी",    // Vaani variant
            "wani",    // English phonetic
            "hey vaani",
            "ok vaani",
            "vaani"    // English
        )
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isEnabled = false

    private var onWakeWordDetectedCallback: ((String) -> Unit)? = null

    fun start(onWakeWordDetected: (String) -> Unit) {
        if (isEnabled) return

        onWakeWordDetectedCallback = onWakeWordDetected
        isEnabled = true

        startListening()
        Log.d(TAG, "Wake word detection started")
    }

    fun stop() {
        isEnabled = false
        stopListening()
        Log.d(TAG, "Wake word detection stopped")
    }

    fun isRunning(): Boolean = isListening

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available")
            return
        }

        if (isListening) {
            speechRecognizer?.stopListening()
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
        Log.d(TAG, "Started listening for wake words")
    }

    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognizer", e)
        }
        isListening = false
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for wake word")
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                if (isEnabled) {
                    startListening()
                }
            }

            override fun onError(error: Int) {
                if (isEnabled) {
                    startListening()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                
                Log.d(TAG, "Heard: $text")
                
                val detectedWakeWord = WAKE_WORDS.find { text.contains(it.lowercase()) }
                
                if (detectedWakeWord != null) {
                    Log.i(TAG, "Wake word detected: $detectedWakeWord")
                    onWakeWordDetectedCallback?.invoke(detectedWakeWord)
                }

                if (isEnabled) {
                    startListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                
                val detectedWakeWord = WAKE_WORDS.find { text.contains(it.lowercase()) }
                
                if (detectedWakeWord != null) {
                    Log.i(TAG, "Wake word partial detected: $detectedWakeWord")
                    onWakeWordDetectedCallback?.invoke(detectedWakeWord)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun destroy() {
        stop()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
