package com.vaani.app.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaani.app.data.local.TaskDao
import com.vaani.app.data.model.AgentState
import com.vaani.app.data.model.AppLanguage
import com.vaani.app.data.model.ExecutionResult
import com.vaani.app.data.model.Task
import com.vaani.app.data.model.TaskEntity
import com.vaani.app.data.model.TaskStatus
import com.vaani.app.data.model.TaskStatusEntity
import com.vaani.app.data.repository.VaaniRepository
import com.vaani.app.service.FloatingVoiceButton
import com.vaani.app.service.PermissionManager
import com.vaani.app.service.TTSManager
import com.vaani.app.service.VaaniService
import com.vaani.app.service.VoiceRecognizer
import com.vaani.app.service.WakeWordDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaaniViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao
) : ViewModel() {

    companion object {
        private const val TAG = "VaaniViewModel"
    }

    private val repository = VaaniRepository

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(false)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _isFloatingButtonEnabled = MutableStateFlow(false)
    val isFloatingButtonEnabled: StateFlow<Boolean> = _isFloatingButtonEnabled.asStateFlow()

    private val _taskHistory = MutableStateFlow<List<Task>>(emptyList())
    val taskHistory: StateFlow<List<Task>> = _taskHistory.asStateFlow()

    val selectedLanguage: StateFlow<AppLanguage> = repository.selectedLanguage
    val isAgentActive: StateFlow<Boolean> = repository.isAgentActive
    val isTTSEnabled: StateFlow<Boolean> = repository.isTTSEnabled
    val micSensitivity: StateFlow<Float> = repository.micSensitivity
    val userName: StateFlow<String> = repository.userName

    val permissionManager = PermissionManager(context)

    private var vaaniService: VaaniService? = null
    private var serviceBound = false

    private var voiceRecognizer: VoiceRecognizer? = null
    private var ttsManager: TTSManager? = null
    private var wakeWordDetector: WakeWordDetector? = null

    private val _lastExecutionResult = MutableSharedFlow<ExecutionResult>()
    val lastExecutionResult = _lastExecutionResult.asSharedFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as VaaniService.LocalBinder
            vaaniService = binder.getService()
            serviceBound = true
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            vaaniService = null
            serviceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    init {
        Log.d(TAG, "ViewModel initialized")
        initializeVoiceComponents()
        loadTasks()
        observeProcessingState()
    }

    private fun initializeVoiceComponents() {
        voiceRecognizer = VoiceRecognizer(context)
        ttsManager = TTSManager(context)
        wakeWordDetector = WakeWordDetector(context)

        ttsManager?.init { success ->
            if (success) {
                Log.d(TAG, "TTS initialized")
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskDao.getAllTasks().collect { entities ->
                _taskHistory.value = entities.map { it.toTask() }
            }
        }
    }

    private fun observeProcessingState() {
        viewModelScope.launch {
            VaaniService.processingState.collect { state ->
                when (state) {
                    is VaaniService.ProcessingState.Idle -> {
                        _agentState.value = AgentState.IDLE
                        updateStatusMessage()
                    }
                    is VaaniService.ProcessingState.Listening -> {
                        _agentState.value = AgentState.LISTENING
                        _statusMessage.value = "Listening..."
                    }
                    is VaaniService.ProcessingState.Analyzing -> {
                        _agentState.value = AgentState.PROCESSING
                        _statusMessage.value = state.message
                    }
                    is VaaniService.ProcessingState.Executing -> {
                        _agentState.value = AgentState.PROCESSING
                        _statusMessage.value = "${state.progress}/${state.total} actions..."
                    }
                    is VaaniService.ProcessingState.Completed -> {
                        _agentState.value = if (state.result.success) AgentState.SUCCESS else AgentState.IDLE
                        _statusMessage.value = if (state.result.success) {
                            "Completed ${state.result.completedActions} actions"
                        } else {
                            state.result.errorMessage ?: "Failed"
                        }

                        _lastExecutionResult.emit(state.result)

                        if (isTTSEnabled.value) {
                            if (state.result.success) {
                                ttsManager?.speakSuccess(selectedLanguage.value)
                            } else {
                                ttsManager?.speakError(selectedLanguage.value)
                            }
                        }

                        viewModelScope.launch {
                            delay(2000)
                            _agentState.value = AgentState.IDLE
                            updateStatusMessage()
                        }
                    }
                    is VaaniService.ProcessingState.Error -> {
                        _agentState.value = AgentState.IDLE
                        _statusMessage.value = state.message

                        if (isTTSEnabled.value) {
                            ttsManager?.speakError(selectedLanguage.value)
                        }
                    }
                }
            }
        }
    }

    fun bindService() {
        Intent(context, VaaniService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }

    fun onVoiceButtonPressed() {
        if (_isListening.value) {
            stopListening()
            return
        }

        if (!permissionManager.hasMicrophonePermission()) {
            _statusMessage.value = "Microphone permission required"
            return
        }

        _isListening.value = true
        _agentState.value = AgentState.LISTENING
        _transcribedText.value = ""
        _statusMessage.value = "Listening..."

        if (isTTSEnabled.value) {
            ttsManager?.speakListening(selectedLanguage.value)
        }

        voiceRecognizer?.startListening(
            language = selectedLanguage.value,
            onResult = { text ->
                _transcribedText.value = text
                _statusMessage.value = "Processing: $text"
                processVoiceInput(text)
            },
            onPartialResult = { partialText ->
                _transcribedText.value = partialText
            },
            onError = { error ->
                Log.e(TAG, "Voice recognition error: $error")
                _isListening.value = false
                _agentState.value = AgentState.IDLE
                _statusMessage.value = error

                viewModelScope.launch {
                    delay(2000)
                    updateStatusMessage()
                }
            },
            onReadyForSpeech = {
                Log.d(TAG, "Ready for speech")
            },
            onEndOfSpeech = {
                _isListening.value = false
            }
        )
    }

    fun stopListening() {
        voiceRecognizer?.stopListening()
        _isListening.value = false

        if (_transcribedText.value.isNotBlank()) {
            _statusMessage.value = "Processing: ${_transcribedText.value}"
            processVoiceInput(_transcribedText.value)
        } else {
            _agentState.value = AgentState.IDLE
            updateStatusMessage()
        }
    }

    fun cancelListening() {
        voiceRecognizer?.stopListening()
        _isListening.value = false
        _transcribedText.value = ""
        _agentState.value = AgentState.IDLE
        updateStatusMessage()
    }

    fun processVoiceInput(text: String) {
        viewModelScope.launch {
            try {
                _agentState.value = AgentState.PROCESSING
                _isListening.value = false

                val languageCode = selectedLanguage.value.code

                val taskId = System.currentTimeMillis().toString()
                val newTask = TaskEntity(
                    id = taskId,
                    description = text,
                    appName = "Processing...",
                    timestamp = System.currentTimeMillis(),
                    status = TaskStatusEntity.IN_PROGRESS.name,
                    language = languageCode
                )
                taskDao.insertTask(newTask)

                val result = if (serviceBound && vaaniService != null) {
                    vaaniService!!.processVoiceInput(text, languageCode)
                } else {
                    Intent(context, VaaniService::class.java).apply {
                        action = VaaniService.ACTION_PROCESS_VOICE
                        putExtra(VaaniService.EXTRA_VOICE_TEXT, text)
                        putExtra(VaaniService.EXTRA_LANGUAGE, languageCode)
                    }
                    context.startForegroundService(this)

                    ExecutionResult(
                        success = true,
                        completedActions = 0,
                        totalActions = 0,
                        errorMessage = null
                    )
                }

                val updatedTask = newTask.copy(
                    appName = "Vaani",
                    status = if (result.success) TaskStatusEntity.DONE.name else TaskStatusEntity.FAILED.name,
                    errorMessage = result.errorMessage
                )
                taskDao.updateTask(updatedTask)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing voice input", e)
                _statusMessage.value = "Error: ${e.message}"
                _agentState.value = AgentState.IDLE
            }
        }
    }

    fun enableWakeWord(enable: Boolean) {
        _isWakeWordEnabled.value = enable

        if (enable) {
            wakeWordDetector?.start { wakeWord ->
                Log.i(TAG, "Wake word detected: $wakeWord")
                onVoiceButtonPressed()
            }
        } else {
            wakeWordDetector?.stop()
        }
    }

    fun enableFloatingButton(enable: Boolean) {
        _isFloatingButtonEnabled.value = enable

        if (enable) {
            FloatingVoiceButton.start(context)
        } else {
            FloatingVoiceButton.stop(context)
        }
    }

    fun setSelectedLanguage(language: AppLanguage) {
        repository.setSelectedLanguage(language)
        updateStatusMessage()
    }

    fun setAgentActive(active: Boolean) {
        repository.setAgentActive(active)

        if (active) {
            VaaniService.startService(context)
        } else {
            VaaniService.stopService(context)
            disableAllVoiceFeatures()
        }
    }

    fun setTTSEnabled(enabled: Boolean) {
        repository.setTTSEnabled(enabled)
        if (!enabled) {
            ttsManager?.stop()
        }
    }

    fun setMicSensitivity(sensitivity: Float) {
        repository.setMicSensitivity(sensitivity)
    }

    fun setUserName(name: String) {
        repository.setUserName(name)
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskDao.deleteTask(taskId)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            taskDao.deleteAllTasks()
        }
    }

    private fun disableAllVoiceFeatures() {
        enableWakeWord(false)
        enableFloatingButton(false)
    }

    private fun updateStatusMessage() {
        _statusMessage.value = when (selectedLanguage.value) {
            AppLanguage.HINDI -> "बोलने के लिए दबाएं"
            AppLanguage.TELUGU -> "మాట్లాడటానికి నдавు"
            AppLanguage.TAMIL -> "பேசுவதற்கு அழுத்தவும்"
            AppLanguage.KANNADA -> "ಮಾತಾಡಲು ಒತ್ತಿ"
            AppLanguage.ENGLISH -> "Hold to speak"
        }
    }

    fun getInitials(): String {
        return userName.value.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "V" }
    }

    private fun TaskEntity.toTask(): Task {
        return Task(
            id = id,
            description = description,
            appName = appName,
            timestamp = timestamp,
            status = try {
                TaskStatus.valueOf(status)
            } catch (e: Exception) {
                TaskStatus.IN_PROGRESS
            },
            language = language
        )
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
        voiceRecognizer?.destroy()
        ttsManager?.shutdown()
        wakeWordDetector?.destroy()
        FloatingVoiceButton.stop(context)
    }
}
