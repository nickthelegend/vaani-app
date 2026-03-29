package com.vaani.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaani.app.core.pipeline.TaskPipeline
import com.vaani.app.core.voice.VoiceRecognizer
import com.vaani.app.data.db.TaskEntity
import com.vaani.app.data.models.AgentState
import com.vaani.app.data.models.AppLanguage
import com.vaani.app.data.repository.TaskRepository
import com.vaani.app.utils.DemoMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaaniViewModel @Inject constructor(
    private val taskPipeline: TaskPipeline,
    private val voiceRecognizer: VoiceRecognizer,
    private val taskRepository: TaskRepository,
    private val demoMode: DemoMode
) : ViewModel() {

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    val partialTranscription = voiceRecognizer.partialText
    val taskHistory = taskRepository.allTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var versionClickCount = 0

    init {
        viewModelScope.launch {
            voiceRecognizer.finalResult.collect { result ->
                result?.let {
                    startTask(it, AppLanguage.ENGLISH) 
                }
            }
        }
    }

    fun toggleListening(language: AppLanguage) {
        if (voiceRecognizer.isListening.value) {
            voiceRecognizer.stopListening()
            _agentState.value = AgentState.IDLE
        } else {
            _agentState.value = AgentState.LISTENING
            voiceRecognizer.startListening(language)
        }
    }

    fun startTask(command: String, language: AppLanguage) {
        viewModelScope.launch {
            _agentState.value = AgentState.PROCESSING
            val result = taskPipeline.executeTask(command, language)
            _agentState.value = if (result.success) AgentState.SUCCESS else AgentState.FAILED
            kotlinx.coroutines.delay(2000)
            _agentState.value = AgentState.IDLE
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            taskRepository.clearAllTasks()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun onVersionClick() {
        versionClickCount++
        if (versionClickCount >= 5) {
            viewModelScope.launch {
                demoMode.runDemoSequence(AppLanguage.ENGLISH)
            }
            versionClickCount = 0
        }
    }
}
