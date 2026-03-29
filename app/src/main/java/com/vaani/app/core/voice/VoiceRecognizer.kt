package com.vaani.app.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.vaani.app.data.models.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "VoiceRecognizer"
    private var speechRecognizer: SpeechRecognizer? = null
    
    val partialText = MutableStateFlow("")
    val finalResult = MutableStateFlow<String?>(null)
    val isListening = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun startListening(language: AppLanguage) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            error.value = "Speech recognition is not available."
            return
        }

        stopListening()
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening.value = true
                    error.value = null
                    partialText.value = ""
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening.value = false
                }

                override fun onError(err: Int) {
                    isListening.value = false
                    val message = when (err) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found."
                        SpeechRecognizer.ERROR_NETWORK -> "Network error."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                        else -> "Error: $err"
                    }
                    error.value = message
                    Log.e(TAG, "Speech Error: $err ($message)")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    finalResult.value = matches?.get(0)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    partialText.value = matches?.get(0) ?: ""
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening.value = false
    }
}
