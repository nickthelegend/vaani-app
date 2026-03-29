package com.vaani.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaani.app.data.models.AgentState
import com.vaani.app.data.models.AppLanguage
import com.vaani.app.ui.components.VoiceButton
import com.vaani.app.ui.theme.*

@Composable
fun FirstTaskScreen(
    language: AppLanguage,
    agentState: AgentState,
    onChipClick: (String) -> Unit,
    onMicClick: () -> Unit,
    onFinish: () -> Unit
) {
    val chips = when (language) {
        AppLanguage.TELUGU -> listOf("వాట్సాప్లో మెసేజ్ పంపు", "Swiggy lo dosa order cheyyi")
        AppLanguage.HINDI -> listOf("YouTube पर गाना बजाओ", "Zomato se pizza mangao")
        AppLanguage.TAMIL -> listOf("Chrome-ல் search செய்", "WhatsApp-ல் மெசேஜ் அனுப்பு")
        else -> listOf("Open WhatsApp", "Play music on YouTube", "Order pizza on Zomato")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "Try it now",
            style = Typography.displayLarge,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap a suggestion or hold the mic",
            style = Typography.bodyLarge,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        chips.forEach { chipText ->
            SuggestionChip(
                text = chipText,
                onClick = { onChipClick(chipText) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        VoiceButton(
            state = agentState,
            onClick = onMicClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (agentState == AgentState.IDLE && agentState != AgentState.LISTENING) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(text = "Start Using Vaani", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = SurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = text, color = TextPrimary, style = Typography.titleLarge)
        }
    }
}
