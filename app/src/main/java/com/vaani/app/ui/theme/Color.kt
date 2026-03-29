package com.vaani.app.ui.theme

import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0A0A0F)
val Surface = Color(0xFF13131A)
val SurfaceVariant = Color(0xFF1C1C28)
val Primary = Color(0xFF6C3FE8)
val PrimaryLight = Color(0xFF9C6FFF)
val PrimaryDark = Color(0xFF4A1FB8)
val Success = Color(0xFF22C55E)
val ErrorColor = Color(0xFFEF4444) // Error is a keyword in some contexts, using ErrorColor
val Warning = Color(0xFFF59E0B)
val TextPrimary = Color(0xFFF0F0F5)
val TextSecondary = Color(0xFF8888AA)
val Border = Color(0xFF2A2A3A)

// Glassmorphism constants
val GlassSurface = Surface.copy(alpha = 0.7f)
val GlassBorder = Border.copy(alpha = 0.5f)
