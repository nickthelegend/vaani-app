package com.vaani.app.ui.screens.settings

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaani.app.data.model.AppLanguage
import com.vaani.app.ui.components.LanguageDropdown
import com.vaani.app.ui.theme.Background
import com.vaani.app.ui.theme.CardBorder
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.Surface
import com.vaani.app.ui.theme.TextPrimary
import com.vaani.app.ui.theme.TextSecondary
import com.vaani.app.viewmodel.VaaniViewModel

@Composable
fun SettingsScreen(
    viewModel: VaaniViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isAgentActive by viewModel.isAgentActive.collectAsState()
    val isTTSEnabled by viewModel.isTTSEnabled.collectAsState()
    val micSensitivity by viewModel.micSensitivity.collectAsState()
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val isFloatingButtonEnabled by viewModel.isFloatingButtonEnabled.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection(
            userName = userName,
            initials = viewModel.getInitials(),
            onNameChange = { viewModel.setUserName(it) },
            selectedLanguage = selectedLanguage,
            onLanguageChange = { viewModel.setSelectedLanguage(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "AI Agent") {
            SettingsToggle(
                title = "Agent Active",
                description = "Enable voice assistant to respond to commands",
                checked = isAgentActive,
                onCheckedChange = { viewModel.setAgentActive(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggle(
                title = "Speak responses",
                description = "Read actions aloud using text-to-speech",
                checked = isTTSEnabled,
                onCheckedChange = { viewModel.setTTSEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LanguageDropdown(
                label = "Response language",
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { viewModel.setSelectedLanguage(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Voice") {
            LanguageDropdown(
                label = "Input language",
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { viewModel.setSelectedLanguage(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MicSensitivitySlider(
                sensitivity = micSensitivity,
                onSensitivityChange = { viewModel.setMicSensitivity(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Hands-Free") {
            SettingsToggle(
                title = "Wake Word",
                description = "Say 'Vaani' to start listening",
                checked = isWakeWordEnabled,
                onCheckedChange = { viewModel.enableWakeWord(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggle(
                title = "Floating Button",
                description = "Show floating mic button on all apps",
                checked = isFloatingButtonEnabled,
                onCheckedChange = { viewModel.enableFloatingButton(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "About") {
            AboutCard()
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ProfileSection(
    userName: String,
    initials: String,
    onNameChange: (String) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Primary, Color(0xFF9C6FFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Primary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LanguageDropdown(
                label = "Language",
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBorder
            )
        )
    }
}

@Composable
private fun MicSensitivitySlider(
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mic sensitivity",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${(sensitivity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sensitivity,
            onValueChange = onSensitivityChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = CardBorder
            )
        )
    }
}

@Composable
private fun AboutCard() {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "App version",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Text(
                text = "1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .animateContentSize(),
            colors = CardDefaults.cardColors(containerColor = Background),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    HowItWorksContent()
                }
            }
        }
    }
}

@Composable
private fun HowItWorksContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArchitectureLayer(
            layerNumber = 1,
            title = "Voice Input Layer",
            description = "Captures your voice using speech recognition and converts it to text in your preferred Indian language."
        )

        ArchitectureLayer(
            layerNumber = 2,
            title = "AI Processing Layer",
            description = "Analyzes your command using AI to understand intent and determines the target app and action to perform."
        )

        ArchitectureLayer(
            layerNumber = 3,
            title = "Automation Layer",
            description = "Uses accessibility services to interact with apps on your behalf, performing the requested actions automatically."
        )
    }
}

@Composable
private fun ArchitectureLayer(
    layerNumber: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = layerNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
