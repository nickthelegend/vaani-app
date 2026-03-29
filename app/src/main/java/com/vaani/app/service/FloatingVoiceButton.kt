package com.vaani.app.service

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.vaani.app.R
import com.vaani.app.data.model.AgentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingVoiceButton : Service() {

    companion object {
        private const val TAG = "FloatingVoiceButton"
        
        const val ACTION_START = "com.vaani.app.START_FLOATING"
        const val ACTION_STOP = "com.vaani.app.STOP_FLOATING"
        const val ACTION_TOGGLE = "com.vaani.app.TOGGLE_FLOATING"

        fun start(context: Context) {
            val intent = Intent(context, FloatingVoiceButton::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingVoiceButton::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            val intent = Intent(context, FloatingVoiceButton::class.java).apply {
                action = ACTION_TOGGLE
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: FrameLayout? = null
    private var isViewAdded = false

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var currentState = AgentState.IDLE
    private var statusHideJob: Job? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "Floating button service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> createFloatingButton()
            ACTION_STOP -> removeFloatingButton()
            ACTION_TOGGLE -> toggleFloatingButton()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removeFloatingButton()
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun createFloatingButton() {
        if (isViewAdded) return

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_voice_button, null) as FrameLayout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 200
        }

        setupTouchListener(floatingView!!)
        setupClickListener(floatingView!!)

        try {
            windowManager?.addView(floatingView, params)
            isViewAdded = true
            Log.d(TAG, "Floating button added")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating button", e)
        }
    }

    private fun removeFloatingButton() {
        if (!isViewAdded || floatingView == null) return

        try {
            windowManager?.removeView(floatingView)
            floatingView = null
            isViewAdded = false
            Log.d(TAG, "Floating button removed")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing floating button", e)
        }
    }

    private fun toggleFloatingButton() {
        if (isViewAdded) {
            removeFloatingButton()
        } else {
            createFloatingButton()
        }
    }

    private fun setupTouchListener(view: View) {
        var moved = false

        view.setOnTouchListener { _, event ->
            val params = view.layoutParams as WindowManager.LayoutParams

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (initialTouchX - event.rawX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        moved = true
                    }

                    params.x = initialX + deltaX
                    params.y = initialY + deltaY

                    windowManager?.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListener(view: View) {
        view.setOnClickListener {
            Log.d(TAG, "Floating button clicked, current state: $currentState")
            when (currentState) {
                AgentState.IDLE -> {
                    updateState(AgentState.LISTENING)
                }
                AgentState.LISTENING -> {
                    updateState(AgentState.PROCESSING)
                }
                else -> {}
            }
        }
    }

    fun updateState(state: AgentState) {
        currentState = state

        val iconView = floatingView?.findViewById<ImageView>(R.id.floating_button_icon)
        val backgroundView = floatingView?.findViewById<View>(R.id.floating_button_background)

        when (state) {
            AgentState.IDLE -> {
                iconView?.setImageResource(android.R.drawable.ic_btn_speak_now)
                backgroundView?.setBackgroundResource(R.drawable.floating_button_idle)
            }
            AgentState.LISTENING -> {
                iconView?.setImageResource(android.R.drawable.ic_btn_speak_now)
                backgroundView?.setBackgroundResource(R.drawable.floating_button_listening)
                startPulseAnimation(backgroundView)
            }
            AgentState.PROCESSING -> {
                iconView?.setImageResource(android.R.drawable.ic_popup_sync)
                backgroundView?.setBackgroundResource(R.drawable.floating_button_processing)
                stopPulseAnimation(backgroundView)
            }
            AgentState.SUCCESS -> {
                iconView?.setImageResource(android.R.drawable.ic_menu_myplaces)
                backgroundView?.setBackgroundResource(R.drawable.floating_button_success)
                scheduleHide()
            }
        }
    }

    private fun startPulseAnimation(view: View?) {
        view?.let {
            ObjectAnimator.ofFloat(it, "alpha", 1f, 0.6f).apply {
                duration = 800
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
        }
    }

    private fun stopPulseAnimation(view: View?) {
        view?.let {
            it.alpha = 1f
        }
    }

    private fun scheduleHide() {
        statusHideJob?.cancel()
        statusHideJob = serviceScope.launch {
            delay(3000)
            updateState(AgentState.IDLE)
        }
    }

    fun showStatus(text: String) {
    }
}
