package com.vaani.app.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.vaani.app.data.model.AppLanguage
import java.util.Locale

class VoiceRecognizer(private val context: Context) {

    companion object {
        private const val TAG = "VoiceRecognizer"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private var onResultCallback: ((String) -> Unit)? = null
    private var onPartialResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onReadyForSpeechCallback: (() -> Unit)? = null
    private var onEndOfSpeechCallback: (() -> Unit)? = null

    private var currentLanguage: AppLanguage = AppLanguage.HINDI

    fun startListening(
        language: AppLanguage,
        onResult: (String) -> Unit,
        onPartialResult: (String) -> Unit,
        onError: (String) -> Unit,
        onReadyForSpeech: () -> Unit = {},
        onEndOfSpeech: () -> Unit = {}
    ) {
        if (isListening) {
            stopListening()
        }

        currentLanguage = language

        onResultCallback = onResult
        onPartialResultCallback = onPartialResult
        onErrorCallback = onError
        onReadyForSpeechCallback = onReadyForSpeech
        onEndOfSpeechCallback = onEndOfSpeech

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available")
            onError("Speech recognition not available on this device")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val intent = createRecognizerIntent(language)
        speechRecognizer?.startListening(intent)
        isListening = true

        Log.d(TAG, "Started listening in ${language.code}")
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping speech recognizer", e)
            }
            isListening = false
        }
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createRecognizerIntent(language: AppLanguage): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
                onReadyForSpeechCallback?.invoke()
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech")
                isListening = false
                onEndOfSpeechCallback?.invoke()
            }

            override fun onError(error: Int) {
                isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "No internet connection"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, try again"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                Log.e(TAG, "Speech recognition error: $error - $errorMessage")
                onErrorCallback?.invoke(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                Log.d(TAG, "Speech recognition results: $text")
                if (text.isNotBlank()) {
                    onResultCallback?.invoke(text)
                } else {
                    onErrorCallback?.invoke("Didn't catch that, try again")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    onPartialResultCallback?.invoke(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "Event: $eventType")
            }
        }
    }

    fun isListening(): Boolean = isListening
}
