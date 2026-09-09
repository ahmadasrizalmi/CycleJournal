package com.app.cyclejournal.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Gradient: Coral to Pink
val PrimaryCoral = Color(0xFFFF8A71)
val PrimaryPink = Color(0xFFFF5E7D)

val CoralPinkGradient = Brush.linearGradient(
    colors = listOf(PrimaryCoral, PrimaryPink)
)

// Canvas & Surfaces
val BackgroundWhite = Color(0xFFFFFFFF)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFFFF8F8)
val BorderSubtle = Color(0xFFF1F5F9)
val BorderCoralTint = Color(0xFFFFE4E6)

// Text Colors
val TextPrimary = Color(0xFF1E293B) // Slate 900
val TextSecondary = Color(0xFF64748B) // Slate 600
val TextDisabled = Color(0xFF94A3B8) // Slate 400

// Clinical Phases Colors
val MenstruationBg = Color(0xFFFFE4E6) // Soft Rose
val MenstruationText = Color(0xFFBE123C) // Dark Rose

val FertileBg = Color(0xFFCFFAFE) // Soft Cyan
val FertileText = Color(0xFF0E7490) // Dark Cyan
val OvulationIndicator = Color(0xFF06B6D4) // Vibrant Cyan Dot

// Clinical Red Flag / Alerts
val AlertBg = Color(0xFFFEF2F2)
val AlertBorder = Color(0xFFEF4444)
val AlertText = Color(0xFF991B1B)
