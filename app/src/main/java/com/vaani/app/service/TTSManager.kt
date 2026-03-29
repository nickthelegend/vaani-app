package com.vaani.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.vaani.app.data.model.AppLanguage
import java.util.Locale
import java.util.UUID

class TTSManager(private val context: Context) {

    companion object {
        private const val TAG = "TTSManager"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false

    private var currentLanguage: AppLanguage = AppLanguage.HINDI
    private var onInitCallback: ((Boolean) -> Unit)? = null

    private val languageToLocale = mapOf(
        AppLanguage.HINDI to Locale("hi", "IN"),
        AppLanguage.TELUGU to Locale("te", "IN"),
        AppLanguage.TAMIL to Locale("ta", "IN"),
        AppLanguage.KANNADA to Locale("kn", "IN"),
        AppLanguage.ENGLISH to Locale("en", "IN")
    )

    private val successResponses = mapOf(
        AppLanguage.HINDI to listOf(
            "आपका काम हो गया",
            "पूरा हो गया",
            "ठीक है",
            "काम पूरा"
        ),
        AppLanguage.TELUGU to listOf(
            "మీ పని పూర్తయింది",
            "పూర్తయ్యింది",
            "అయిపोతుంది",
            "చేశాను"
        ),
        AppLanguage.TAMIL to listOf(
            "உங்கள் பணி முடிந்தது",
            "முடிந்தது",
            "பூர்த்தி ஆயிற்று",
            "செய்துவிட்டேன்"
        ),
        AppLanguage.KANNADA to listOf(
            "ನಿಮ್ಮ ಕೆಲಸ ಮುಗಿದಿದೆ",
            "ಮುಗಿದಿದೆ",
            "ಮಾಡಿದ್ದೇನೆ",
            "ಪೂರ್ಣವಾಯಿತು"
        ),
        AppLanguage.ENGLISH to listOf(
            "Done! Your task has been completed.",
            "Task completed successfully.",
            "All done!",
            "I've finished the task."
        )
    )

    private val errorResponses = mapOf(
        AppLanguage.HINDI to listOf(
            "माफ़ करना, काम नहीं हो सका",
            "कोई error हो गई",
            "फिर से कोशिश करो"
        ),
        AppLanguage.TELUGU to listOf(
            "క్షमా, పని లేదు",
            "ఏదో జ happened happened",
            "మళ్ళी प्रयास करు"
        ),
        AppLanguage.TAMIL to listOf(
            "மன்னிக்கவும், பணி முடியவில்லை",
            "ஏதோ பிரச்சினை",
            "மீண்டும் முயற்சி செய்"
        ),
        AppLanguage.KANNADA to listOf(
            "ಕ್ಷಮಿಸಿ, ಕೆಲಸ ಆಗಲಿಲ್ಲ",
            "ಏನೋ ದೋಷ ಆಯ್ತು",
            "ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ"
        ),
        AppLanguage.ENGLISH to listOf(
            "Sorry, couldn't complete the task.",
            "Something went wrong.",
            "Please try again."
        )
    )

    private val listeningResponses = mapOf(
        AppLanguage.HINDI to "सुन रहा हूं",
        AppLanguage.TELUGU to "వింటున్నాను",
        AppLanguage.TAMIL to "கேட்கிறேன்",
        AppLanguage.KANNADA to "ಕೇಳುತ್ತಿದ್ದೇನೆ",
        AppLanguage.ENGLISH to "Listening..."
    )

    fun init(onReady: (Boolean) -> Unit = {}) {
        onInitCallback = onReady

        textToSpeech = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                Log.d(TAG, "TTS initialized successfully")
                setupTTS()
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
            onReady(isInitialized)
        }
    }

    private fun setupTTS() {
        textToSpeech?.apply {
            setPitch(1.1f)
            setSpeechRate(0.95f)

            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                    Log.d(TAG, "TTS started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    Log.d(TAG, "TTS done: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    Log.e(TAG, "TTS error: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    isSpeaking = false
                    Log.e(TAG, "TTS error: $utteranceId, code: $errorCode")
                }
            })
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized")
            return
        }

        currentLanguage = language

        val locale = languageToLocale[language] ?: Locale.ENGLISH
        val result = textToSpeech?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language not supported: ${language.code}, falling back to English")
            textToSpeech?.setLanguage(Locale.ENGLISH)
        }

        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        Log.d(TAG, "Speaking: $text")
    }

    fun speakSuccess(language: AppLanguage) {
        val responses = successResponses[language] ?: successResponses[AppLanguage.ENGLISH]!!
        speak(responses.random(), language)
    }

    fun speakError(language: AppLanguage) {
        val responses = errorResponses[language] ?: errorResponses[AppLanguage.ENGLISH]!!
        speak(responses.random(), language)
    }

    fun speakListening(language: AppLanguage) {
        val text = listeningResponses[language] ?: listeningResponses[AppLanguage.ENGLISH]!!
        speak(text, language)
    }

    fun speakCustom(text: String, language: AppLanguage) {
        speak(text, language)
    }

    fun stop() {
        if (isSpeaking) {
            textToSpeech?.stop()
            isSpeaking = false
            Log.d(TAG, "TTS stopped")
        }
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        Log.d(TAG, "TTS shutdown")
    }

    fun isSpeaking(): Boolean = isSpeaking

    fun isInitialized(): Boolean = isInitialized
}
