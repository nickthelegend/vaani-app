package com.vaani.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vaani.app.data.models.AgentState
import com.vaani.app.ui.theme.*

@Composable
fun VoiceButton(
    state: AgentState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == AgentState.IDLE) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Ripple rings for listening state
        if (state == AgentState.LISTENING) {
            RippleRings()
        }

        Surface(
            modifier = Modifier
                .size(100.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = Color.Transparent,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = when (state) {
                                AgentState.SUCCESS -> listOf(Success, Success.copy(alpha = 0.7f))
                                AgentState.FAILED -> listOf(ErrorColor, ErrorColor.copy(alpha = 0.7f))
                                else -> listOf(Primary, PrimaryDark)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state == AgentState.SUCCESS) Icons.Default.Check else Icons.Default.Mic,
                    contentDescription = "Voice Button",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                
                if (state == AgentState.PROCESSING) {
                    // Spinning arc would go here, using a simpler CircularProgressIndicator for now or custom canvas
                }
            }
        }
    }
}

@Composable
fun RippleRings() {
    val infiniteTransition = rememberInfiniteTransition(label = "ripples")
    
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(rippleScale1)
            .background(Primary.copy(alpha = rippleAlpha1), CircleShape)
    )
}
