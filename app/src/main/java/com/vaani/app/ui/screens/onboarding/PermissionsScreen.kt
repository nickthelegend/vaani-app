package com.vaani.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaani.app.ui.components.PermissionCard
import com.vaani.app.ui.theme.*

@Composable
fun PermissionsScreen(
    micGranted: Boolean,
    overlayGranted: Boolean,
    accessibilityGranted: Boolean,
    onRequestMic: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "3 quick permissions",
            style = Typography.displayLarge,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Card 1: Microphone
        PermissionCard(
            title = "Microphone",
            description = "To hear your voice commands",
            granted = micGranted,
            icon = Icons.Default.Mic,
            onClick = onRequestMic
        )

        // Card 2: Overlay
        PermissionCard(
            title = "Overlay",
            description = "For the floating button over other apps",
            granted = overlayGranted,
            icon = Icons.Default.Layers,
            onClick = onRequestOverlay
        )

        // Card 3: Accessibility
        PermissionCard(
            title = "Accessibility",
            description = "To control your apps and perform actions",
            granted = accessibilityGranted,
            icon = Icons.Default.Accessibility,
            onClick = onRequestAccessibility
        )

        Spacer(modifier = Modifier.weight(1f))

        if (accessibilityGranted && !micGranted || !overlayGranted) {
             // Show helper tip for accessibility if needed
        }

        Button(
            onClick = onContinue,
            enabled = micGranted && overlayGranted && accessibilityGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Color.White,
                disabledContainerColor = SurfaceVariant,
                disabledContentColor = TextSecondary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Continue", fontWeight = FontWeight.Bold)
        }
    }
}
