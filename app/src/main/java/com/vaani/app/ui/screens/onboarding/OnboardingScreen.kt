package com.vaani.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaani.app.data.models.AppLanguage
import com.vaani.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(logoScale)
                .background(
                    brush = Brush.verticalGradient(listOf(PrimaryLight, Primary)),
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title cycling through languages
        ScrollingTitle()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your AI Phone Agent",
            style = Typography.displayLarge,
            color = TextPrimary
        )
        Text(
            text = "Speak in your language. We handle the rest.",
            style = Typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Language Selector
        Text(
            text = "Select your language",
            style = Typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(AppLanguage.entries) { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onSelect = { 
                        selectedLanguage = it
                        onLanguageSelected(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            enabled = selectedLanguage != null,
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
            Text(text = "Get Started", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LanguageCard(
    language: AppLanguage,
    isSelected: Boolean,
    onSelect: (AppLanguage) -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else SurfaceVariant)
            .border(
                1.dp,
                if (isSelected) Primary else Border.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect(language) }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = language.nativeScript, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(text = language.displayName, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun ScrollingTitle() {
    val languages = listOf("वाणी", "వాాణి", "வாணி", "ವಾನಿ", "Vaani")
    var index by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            index = (index + 1) % languages.size
        }
    }

    AnimatedContent(
        targetState = languages[index],
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
        },
        label = "title_anim"
    ) { text ->
        Text(text = text, color = PrimaryLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
