package com.vaani.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vaani.app.ui.screens.home.HomeScreen
import com.vaani.app.ui.screens.history.HistoryScreen
import com.vaani.app.ui.screens.settings.SettingsScreen
import com.vaani.app.ui.screens.onboarding.OnboardingScreen
import com.vaani.app.ui.screens.onboarding.PermissionsScreen
import com.vaani.app.ui.screens.onboarding.FirstTaskScreen
import com.vaani.app.ui.theme.VaaniTheme
import com.vaani.app.viewmodel.VaaniViewModel
import com.vaani.app.viewmodel.OnboardingViewModel
import com.vaani.app.core.accessibility.VaaniAccessibilityService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vaaniViewModel: VaaniViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onboardingViewModel.onMicPermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            VaaniTheme {
                val navController = rememberNavController()
                val onboardingFinished by onboardingViewModel.onboardingFinished.collectAsState()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (onboardingFinished) "home" else "onboarding_welcome"
                    ) {
                        composable("onboarding_welcome") {
                            OnboardingScreen(
                                onLanguageSelected = { onboardingViewModel.setLanguage(it) },
                                onContinue = { navController.navigate("onboarding_permissions") }
                            )
                        }
                        
                        composable("onboarding_permissions") {
                            val micGranted by onboardingViewModel.micGranted.collectAsState()
                            val overlayGranted by onboardingViewModel.overlayGranted.collectAsState()
                            val accessibilityGranted by onboardingViewModel.accessibilityGranted.collectAsState()
                            
                            PermissionsScreen(
                                micGranted = micGranted,
                                overlayGranted = overlayGranted,
                                accessibilityGranted = accessibilityGranted,
                                onRequestMic = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                onRequestOverlay = { requestOverlayPermission() },
                                onRequestAccessibility = { requestAccessibilityPermission() },
                                onContinue = { navController.navigate("onboarding_demo") }
                            )
                        }
                        
                        composable("onboarding_demo") {
                            val language by onboardingViewModel.selectedLanguage.collectAsState()
                            val agentState by vaaniViewModel.agentState.collectAsState()
                            
                            FirstTaskScreen(
                                language = language,
                                agentState = agentState,
                                onChipClick = { vaaniViewModel.startTask(it, language) },
                                onMicClick = { vaaniViewModel.toggleListening(language) },
                                onFinish = { 
                                    onboardingViewModel.finishOnboarding()
                                    navController.navigate("home") {
                                        popUpTo("onboarding_welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val state by vaaniViewModel.agentState.collectAsState()
                            val language by onboardingViewModel.selectedLanguage.collectAsState()
                            val partialText by vaaniViewModel.partialTranscription.collectAsState()
                            
                            HomeScreen(
                                state = state,
                                language = language,
                                partialText = partialText,
                                onMicClick = { vaaniViewModel.toggleListening(language) },
                                onHistoryClick = { navController.navigate("history") },
                                onSettingsClick = { navController.navigate("settings") },
                                onLanguageClick = { /* Change language dialog */ }
                            )
                        }
                        
                        composable("history") {
                            val tasks by vaaniViewModel.taskHistory.collectAsState()
                            HistoryScreen(
                                tasks = tasks,
                                onBackClick = { navController.popBackStack() },
                                onClearAll = { vaaniViewModel.clearHistory() },
                                onDeleteTask = { vaaniViewModel.deleteTask(it) }
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() },
                                onLangClick = { /* Change language */ },
                                voiceActive = true,
                                onVoiceToggle = { /* Toggle */ },
                                floatingButton = true,
                                onFloatingToggle = { /* Toggle */ },
                                onVersionClick = { vaaniViewModel.onVersionClick() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        onboardingViewModel.checkPermissions(this)
    }
}
