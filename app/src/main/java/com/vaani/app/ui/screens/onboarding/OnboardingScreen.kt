package com.vaani.app.ui.screens.onboarding

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaani.app.data.model.AppLanguage
import com.vaani.app.ui.theme.Background
import com.vaani.app.ui.theme.CardBorder
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.PrimaryLight
import com.vaani.app.ui.theme.Success
import com.vaani.app.ui.theme.Surface
import com.vaani.app.ui.theme.TextPrimary
import com.vaani.app.ui.theme.TextSecondary
import com.vaani.app.viewmodel.VaaniViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    viewModel: VaaniViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (currentStep) {
            0 -> WelcomeStep(
                selectedLanguage = viewModel.selectedLanguage.value,
                onLanguageSelected = { viewModel.setSelectedLanguage(it) },
                onGetStarted = { currentStep = 1 }
            )
            1 -> PermissionsStep(
                viewModel = viewModel,
                context = context,
                onComplete = { currentStep = 2 }
            )
            2 -> FirstTaskStep(
                viewModel = viewModel,
                onComplete = onComplete
            )
        }
    }
}

@Composable
private fun WelcomeStep(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onGetStarted: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, PrimaryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Meet Vaani",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your AI phone agent.\nSpeak in your language.\nWe handle the rest.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AppLanguage.entries) { language ->
                LanguageCard(
                    language = language,
                    isSelected = language == selectedLanguage,
                    onClick = { onLanguageSelected(language) }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Primary, RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = language.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Primary else TextPrimary
            )
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PermissionsStep(
    viewModel: VaaniViewModel,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    var microphoneGranted by remember { mutableStateOf(viewModel.permissionManager.hasMicrophonePermission()) }
    var overlayGranted by remember { mutableStateOf(viewModel.permissionManager.hasOverlayPermission()) }
    var accessibilityGranted by remember { mutableStateOf(viewModel.permissionManager.hasAccessibilityPermission()) }
    var allGranted by remember { mutableStateOf(false) }

    val activity = context as? Activity

    LaunchedEffect(microphoneGranted, overlayGranted, accessibilityGranted) {
        while (!allGranted) {
            microphoneGranted = viewModel.permissionManager.hasMicrophonePermission()
            overlayGranted = viewModel.permissionManager.hasOverlayPermission()
            accessibilityGranted = viewModel.permissionManager.hasAccessibilityPermission()
            allGranted = microphoneGranted && overlayGranted && accessibilityGranted
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vaani needs these permissions to work",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionCard(
            icon = "🎤",
            title = "Microphone",
            description = "To hear your voice commands",
            isGranted = microphoneGranted,
            onGrant = {
                activity?.let {
                    viewModel.permissionManager.requestMicrophonePermission(it)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            icon = "🪟",
            title = "Display Over Apps",
            description = "To show floating button on all apps",
            isGranted = overlayGranted,
            onGrant = {
                activity?.let {
                    viewModel.permissionManager.requestOverlayPermission(it)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            icon = "🔧",
            title = "Accessibility Service",
            description = "To control apps and perform actions",
            isGranted = accessibilityGranted,
            onGrant = {
                activity?.let {
                    viewModel.permissionManager.openAccessibilitySettings(it)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            enabled = allGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (allGranted) "All Set!" else "Grant All Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionCard(
    icon: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted, onClick = onGrant),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Success.copy(alpha = 0.1f) else Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) Success else TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Success),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Granted",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FirstTaskStep(
    viewModel: VaaniViewModel,
    onComplete: () -> Unit
) {
    val selectedLanguage = viewModel.selectedLanguage.value

    val examplePrompts = when (selectedLanguage) {
        AppLanguage.TELUGU -> listOf(
            "WhatsApp-lo ammayiki message pothu" to "WhatsApp-lo ammayiki message pothu",
            "YouTube-lo songs choodu" to "YouTube-lo songs choodu"
        )
        AppLanguage.HINDI -> listOf(
            "WhatsApp par mummy ko message bhejo" to "WhatsApp par mummy ko message bhejo",
            "Swiggy par chai order karo" to "Swiggy par chai order karo"
        )
        AppLanguage.TAMIL -> listOf(
            "WhatsApp-la amma-ku message podu" to "WhatsApp-la amma-ku message podu",
            "YouTube-la songs paathu" to "YouTube-la songs paathu"
        )
        AppLanguage.KANNADA -> listOf(
            "WhatsApp-ದಲ್ಲಿ ತಾಯಿಗೆ ಸಂದೇಶ ಕಳುಹಿಸು" to "WhatsApp-ದಲ್ಲಿ ತಾಯಿಗೆ ಸಂದೇಶ ಕಳುಹಿಸು",
            "YouTube-ದಲ್ಲಿ songs ನೋಡು" to "YouTube-ದಲ್ಲಿ songs ನೋಡು"
        )
        AppLanguage.ENGLISH -> listOf(
            "Send message to mom on WhatsApp" to "Send message to mom on WhatsApp",
            "Play songs on YouTube" to "Play songs on YouTube"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Try Saying Something",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap an example or speak your command",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "mic")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "micScale"
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary, PrimaryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        examplePrompts.forEach { (native, transliteration) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.processVoiceInput(native)
                        onComplete()
                    },
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = native,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    if (native != transliteration) {
                        Text(
                            text = transliteration,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Skip for Now",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
