package com.vaani.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.vaani.app.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VaaniRepository {

    private const val PREFS_NAME = "vaani_prefs"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"
    private const val KEY_AGENT_ACTIVE = "agent_active"
    private const val KEY_TTS_ENABLED = "tts_enabled"
    private const val KEY_MIC_SENSITIVITY = "mic_sensitivity"
    private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
    private const val KEY_FLOATING_BUTTON_ENABLED = "floating_button_enabled"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _selectedLanguage = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString(KEY_SELECTED_LANGUAGE, "hi-IN") } ?: AppLanguage.HINDI
    )
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _isAgentActive = MutableStateFlow(prefs.getBoolean(KEY_AGENT_ACTIVE, true))
    val isAgentActive: StateFlow<Boolean> = _isAgentActive.asStateFlow()

    private val _isTTSEnabled = MutableStateFlow(prefs.getBoolean(KEY_TTS_ENABLED, true))
    val isTTSEnabled: StateFlow<Boolean> = _isTTSEnabled.asStateFlow()

    private val _micSensitivity = MutableStateFlow(prefs.getFloat(KEY_MIC_SENSITIVITY, 0.7f))
    val micSensitivity: StateFlow<Float> = _micSensitivity.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Vaani User") ?: "Vaani User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(prefs.getBoolean(KEY_WAKE_WORD_ENABLED, false))
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _isFloatingButtonEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLOATING_BUTTON_ENABLED, false))
    val isFloatingButtonEnabled: StateFlow<Boolean> = _isFloatingButtonEnabled.asStateFlow()

    fun isOnboardingComplete(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    fun setSelectedLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        prefs.edit().putString(KEY_SELECTED_LANGUAGE, language.code).apply()
    }

    fun setAgentActive(active: Boolean) {
        _isAgentActive.value = active
        prefs.edit().putBoolean(KEY_AGENT_ACTIVE, active).apply()
    }

    fun setTTSEnabled(enabled: Boolean) {
        _isTTSEnabled.value = enabled
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
    }

    fun setMicSensitivity(sensitivity: Float) {
        _micSensitivity.value = sensitivity
        prefs.edit().putFloat(KEY_MIC_SENSITIVITY, sensitivity).apply()
    }

    fun setUserName(name: String) {
        _userName.value = name
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _isWakeWordEnabled.value = enabled
        prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        _isFloatingButtonEnabled.value = enabled
        prefs.edit().putBoolean(KEY_FLOATING_BUTTON_ENABLED, enabled).apply()
    }
}
