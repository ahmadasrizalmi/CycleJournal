package com.app.cyclejournal.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Exact Figma/Tailwind Tokens from HTML prototype & design specification
val Coral300 = Color(0xFFFFB2A1)
val Coral400 = Color(0xFFFF8A71)
val Coral500 = Color(0xFFFF6F61)
val Coral600 = Color(0xFFFF5E7D)
val Coral700 = Color(0xFFE64264)

val MedicalCyan = Color(0xFF06B6D4)
val MedicalTeal = Color(0xFF0D9488)
val MedicalRose = Color(0xFFF43F5E)
val MedicalAmber = Color(0xFFF59E0B)
val MedicalSlate = Color(0xFF0F172A)

val LightBackground = Color(0xFFFBFBFC)
val DarkBackground = Color(0xFF0B0F19)
val DarkCardBackground = Color(0xFF151D2E)
val DarkBorder = Color(0xFF243048)
val DarkBorderColor = DarkBorder

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Gradients
val CoralLinearGradient = Brush.linearGradient(
    colors = listOf(Coral400, Coral600)
)
val SoftCoralGradient = Brush.linearGradient(
    colors = listOf(Coral400.copy(alpha = 0.12f), Coral600.copy(alpha = 0.12f))
)
val AmberBadgeGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
)
val FertileCyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF14B8A6), Color(0xFF06B6D4))
)
val MenstrualRoseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
)
val PearlIridescentGradient = Brush.radialGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFEE2E2), Color(0xFFCBD5E1))
)

// Legacy aliases for backward compatibility
val PrimaryCoral = Coral400
val PrimaryPink = Coral600
val CoralPinkGradient = CoralLinearGradient
val BackgroundWhite = Color(0xFFFFFFFF)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFFFF8F8)
val BorderSubtle = Color(0xFFF1F5F9)
val BorderCoralTint = Color(0xFFFFE4E6)
val TextPrimary = Slate800
val TextSecondary = Slate500
val TextDisabled = Slate400
val MenstruationBg = Color(0xFFFFE4E6)
val MenstruationText = Color(0xFFBE123C)
val FertileBg = Color(0xFFCFFAFE)
val FertileText = Color(0xFF0E7490)
val OvulationIndicator = MedicalCyan
val AlertBg = Color(0xFFFEF2F2)
val AlertBorder = Color(0xFFEF4444)
val AlertText = Color(0xFF991B1B)
