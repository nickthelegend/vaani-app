package com.vaani.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vaani.app.MainActivity
import com.vaani.app.R
import com.vaani.app.data.model.AgentAction
import com.vaani.app.data.model.ExecutionResult
import com.vaani.app.data.model.ScreenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaaniService : Service() {

    companion object {
        private const val TAG = "VaaniService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "vaani_service_channel"
        private const val CHANNEL_NAME = "Vaani Service"

        const val ACTION_STOP = "com.vaani.app.STOP_SERVICE"
        const val ACTION_PROCESS_VOICE = "com.vaani.app.PROCESS_VOICE"
        const val EXTRA_VOICE_TEXT = "voice_text"
        const val EXTRA_LANGUAGE = "language"

        private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
        val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

        private val _executionResult = MutableSharedFlow<ExecutionResult>()
        val executionResult: SharedFlow<ExecutionResult> = _executionResult.asSharedFlow()

        fun startService(context: Context) {
            val intent = Intent(context, VaaniService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VaaniService::class.java)
            context.stopService(intent)
        }
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var geminiClient: GeminiClient
    private lateinit var actionExecutor: ActionExecutor

    sealed class ProcessingState {
        data object Idle : ProcessingState()
        data object Listening : ProcessingState()
        data class Analyzing(val message: String = "Analyzing screen...") : ProcessingState()
        data class Executing(val action: String, val progress: Int, val total: Int) : ProcessingState()
        data class Completed(val result: ExecutionResult) : ProcessingState()
        data class Error(val message: String) : ProcessingState()
    }

    inner class LocalBinder : Binder() {
        fun getService(): VaaniService = this@VaaniService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        geminiClient = GeminiClient()
        actionExecutor = ActionExecutor()
        
        VaaniAccessibilityService.instance?.let { a11yService ->
            actionExecutor.setAccessibilityService(a11yService)
            Log.d(TAG, "ActionExecutor connected to accessibility service")
        }
        
        VaaniAccessibilityService.isServiceRunning.value.let { running ->
            Log.d(TAG, "Accessibility service running: $running")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with intent: $intent")

        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PROCESS_VOICE -> {
                val voiceText = intent.getStringExtra(EXTRA_VOICE_TEXT) ?: return START_NOT_STICKY
                val language = intent.getStringExtra(EXTRA_LANGUAGE) ?: "hi-IN"
                serviceScope.launch {
                    processVoiceInput(voiceText, language)
                }
            }
        }

        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val stopIntent = Intent(this, VaaniService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Vaani")
            .setContentText("Voice assistant is active")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Vaani voice assistant service"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun processVoiceInput(spokenText: String, language: String): ExecutionResult {
        Log.d(TAG, "Processing voice input: $spokenText, language: $language")
        
        _processingState.value = ProcessingState.Listening

        try {
            _processingState.value = ProcessingState.Analyzing("Getting screen info...")
            
            val screenNode = VaaniAccessibilityService.screenState.value 
                ?: VaaniAccessibilityService.getCurrentScreen()
                ?: throw Exception("Could not get screen information")

            _processingState.value = ProcessingState.Analyzing("Planning actions...")

            val targetPackage = VaaniAccessibilityService.getCurrentPackage()
            val actions = geminiClient.generateActions(
                screenNode = screenNode,
                userCommand = spokenText
            )

            if (actions.isEmpty()) {
                val errorResult = ExecutionResult(
                    success = false,
                    completedActions = 0,
                    totalActions = 0,
                    errorMessage = "Could not understand the command"
                )
                _processingState.value = ProcessingState.Completed(errorResult)
                _executionResult.emit(errorResult)
                return errorResult
            }

            _processingState.value = ProcessingState.Executing(
                action = "Starting execution...",
                progress = 0,
                total = actions.size
            )

            val result = actionExecutor.execute(actions)

            _processingState.value = ProcessingState.Completed(result)
            _executionResult.emit(result)

            Log.d(TAG, "Execution completed: ${result.success}, ${result.completedActions}/${result.totalActions}")
            return result

        } catch (e: Exception) {
            Log.e(TAG, "Error processing voice input", e)
            val errorResult = ExecutionResult(
                success = false,
                completedActions = 0,
                totalActions = 0,
                errorMessage = e.message ?: "Unknown error"
            )
            _processingState.value = ProcessingState.Error(e.message ?: "Unknown error")
            _executionResult.emit(errorResult)
            return errorResult
        }
    }
}
