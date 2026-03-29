package com.vaani.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaani.app.data.models.AgentState
import com.vaani.app.data.models.AppLanguage
import com.vaani.app.ui.components.VoiceButton
import com.vaani.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AgentState,
    language: AppLanguage,
    partialText: String,
    onMicClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Vaani", style = Typography.displayLarge, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    LanguagePill(language, onClick = onLanguageClick)
                }
            )
        },
        bottomBar = {
            HomeBottomBar(onHistoryClick, onSettingsClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatusText(state)

            Spacer(modifier = Modifier.height(48.dp))

            VoiceButton(state, onClick = onMicClick)

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = state == AgentState.LISTENING || state == AgentState.PROCESSING,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                TranscriptionCard(partialText, isProcessing = state == AgentState.PROCESSING)
            }
        }
    }
}

@Composable
fun StatusText(state: AgentState) {
    val text = when (state) {
        AgentState.IDLE -> "Tap to speak"
        AgentState.LISTENING -> "Listening..."
        AgentState.PROCESSING -> "Working on it..."
        AgentState.SUCCESS -> "Task completed!"
        AgentState.FAILED -> "Sorry, try again"
    }
    
    val color = when (state) {
        AgentState.SUCCESS -> Success
        AgentState.FAILED -> ErrorColor
        else -> TextPrimary
    }

    Text(
        text = text,
        style = Typography.titleLarge,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun TranscriptionCard(text: String, isProcessing: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceVariant,
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (text.isEmpty() && !isProcessing) "..." else text,
                style = Typography.headlineMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun LanguagePill(language: AppLanguage, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = language.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HomeBottomBar(onHistoryClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(64.dp)
            .background(SurfaceVariant, RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onHistoryClick) {
            Icon(Icons.Default.History, contentDescription = "History", tint = TextSecondary)
        }
        
        Spacer(modifier = Modifier.width(32.dp))

        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
        }
    }
}
