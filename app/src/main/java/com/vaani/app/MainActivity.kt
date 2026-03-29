package com.vaani.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vaani.app.data.repository.VaaniRepository
import com.vaani.app.ui.navigation.Screen
import com.vaani.app.ui.screens.history.HistoryScreen
import com.vaani.app.ui.screens.home.HomeScreen
import com.vaani.app.ui.screens.onboarding.OnboardingScreen
import com.vaani.app.ui.screens.settings.SettingsScreen
import com.vaani.app.ui.theme.Background
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.Surface
import com.vaani.app.ui.theme.TextSecondary
import com.vaani.app.ui.theme.VaaniTheme
import com.vaani.app.viewmodel.VaaniViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaaniTheme {
                VaaniApp()
            }
        }
    }
}

@Composable
fun VaaniApp() {
    val navController = rememberNavController()
    val viewModel: VaaniViewModel = viewModel()
    
    val isOnboardingComplete by remember { mutableStateOf(VaaniRepository.isOnboardingComplete()) }
    var showOnboarding by remember { mutableStateOf(!isOnboardingComplete) }

    if (showOnboarding) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                VaaniRepository.setOnboardingComplete(true)
                showOnboarding = false
                
                if (VaaniRepository.isFloatingButtonEnabled.value) {
                    viewModel.enableFloatingButton(true)
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = Surface,
                    contentColor = Color.White
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(text = screen.title)
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel = viewModel)
                }
                composable(Screen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
