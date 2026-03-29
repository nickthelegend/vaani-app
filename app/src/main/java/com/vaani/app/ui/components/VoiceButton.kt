package com.vaani.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vaani.app.data.model.AgentState
import com.vaani.app.ui.theme.Error
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.PrimaryLight
import com.vaani.app.ui.theme.Success

@Composable
fun VoiceButton(
    agentState: AgentState,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voiceButton")

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val buttonScale by animateFloatAsState(
        targetValue = when (agentState) {
            AgentState.LISTENING -> 1.05f
            AgentState.PROCESSING -> 1.0f
            AgentState.SUCCESS -> 1.1f
            AgentState.IDLE -> 1.0f
        },
        animationSpec = tween(300),
        label = "buttonScale"
    )

    val gradientColors = when (agentState) {
        AgentState.IDLE -> listOf(Primary, PrimaryLight)
        AgentState.LISTENING -> listOf(Error, Error.copy(alpha = 0.7f))
        AgentState.PROCESSING -> listOf(Primary, PrimaryLight)
        AgentState.SUCCESS -> listOf(Success, Success.copy(alpha = 0.7f))
    }

    val iconColor = when (agentState) {
        AgentState.IDLE -> Color.White
        AgentState.LISTENING -> Color.White
        AgentState.PROCESSING -> Color.White
        AgentState.SUCCESS -> Color.White
    }

    Box(
        modifier = modifier
            .size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        if (agentState == AgentState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(rippleScale)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = rippleAlpha))
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(rippleScale * 0.8f)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = rippleAlpha * 0.5f))
            )
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = gradientColors
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPress()
                            tryAwaitRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (agentState) {
                    AgentState.IDLE -> Icons.Filled.Mic
                    AgentState.LISTENING -> Icons.Filled.Mic
                    AgentState.PROCESSING -> Icons.Filled.MicOff
                    AgentState.SUCCESS -> Icons.Filled.Check
                },
                contentDescription = "Voice Button",
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
