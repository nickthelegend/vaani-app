package com.vaani.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vaani.app.data.model.AgentState
import com.vaani.app.data.model.Task
import com.vaani.app.ui.components.AppIconPlaceholder
import com.vaani.app.ui.components.LanguageSelector
import com.vaani.app.ui.components.StatusChip
import com.vaani.app.ui.components.VoiceButton
import com.vaani.app.ui.theme.Background
import com.vaani.app.ui.theme.CardBorder
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.Success
import com.vaani.app.ui.theme.Surface
import com.vaani.app.ui.theme.TextPrimary
import com.vaani.app.ui.theme.TextSecondary
import com.vaani.app.viewmodel.VaaniViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VaaniViewModel,
    modifier: Modifier = Modifier
) {
    val agentState by viewModel.agentState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val taskHistory by viewModel.taskHistory.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val transcribedText by viewModel.transcribedText.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var lastTask by remember { mutableStateOf<Task?>(null) }

    DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose {
            viewModel.unbindService()
        }
    }

    LaunchedEffect(agentState) {
        if (agentState == AgentState.SUCCESS) {
            delay(2000)
            if (taskHistory.isNotEmpty()) {
                lastTask = taskHistory.first()
                showBottomSheet = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vaani",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Your AI Phone Agent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { viewModel.setSelectedLanguage(it) }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            VoiceButtonWithStatus(
                agentState = agentState,
                transcribedText = transcribedText,
                isListening = isListening,
                onPress = { viewModel.onVoiceButtonPressed() },
                onCancel = { viewModel.cancelListening() }
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = isListening && transcribedText.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TranscriptionCard(
                    text = transcribedText,
                    onCancel = { viewModel.cancelListening() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showBottomSheet && lastTask != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Surface,
            contentColor = TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextSecondary)
                )
            }
        ) {
            LastTaskBottomSheet(
                task = lastTask!!,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun VoiceButtonWithStatus(
    agentState: AgentState,
    transcribedText: String,
    isListening: Boolean,
    onPress: () -> Unit,
    onCancel: () -> Unit
) {
    val displayState = if (isListening) AgentState.LISTENING else agentState

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            VoiceButton(
                agentState = displayState,
                onPress = onPress
            )

            AnimatedVisibility(
                visible = isListening,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StatusText(
            agentState = displayState,
            statusMessage = if (transcribedText.isNotBlank() && isListening) transcribedText else getStatusText(displayState)
        )
    }
}

@Composable
private fun StatusText(
    agentState: AgentState,
    statusMessage: String
) {
    val textColor by animateColorAsState(
        targetValue = when (agentState) {
            AgentState.IDLE -> TextSecondary
            AgentState.LISTENING -> Primary
            AgentState.PROCESSING -> Primary
            AgentState.SUCCESS -> Success
        },
        animationSpec = tween(300),
        label = "textColor"
    )

    Text(
        text = statusMessage,
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

@Composable
private fun TranscriptionCard(
    text: String,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LastTaskBottomSheet(
    task: Task,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Last Action",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Background)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconPlaceholder(appName = task.appName)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.appName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(12.dp),
                        tint = TextSecondary
                    )

                    Text(
                        text = formatTimestamp(task.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusChip(status = task.status)
        }
    }
}

private fun getStatusText(agentState: AgentState): String {
    return when (agentState) {
        AgentState.IDLE -> "Hold to speak"
        AgentState.LISTENING -> "Listening..."
        AgentState.PROCESSING -> "Working on it..."
        AgentState.SUCCESS -> "Done!"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
