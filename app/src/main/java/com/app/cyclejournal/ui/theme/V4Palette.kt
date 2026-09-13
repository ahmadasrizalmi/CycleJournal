package com.app.cyclejournal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Every colour the v4 design system uses, in one swappable object.
 *
 * The light instance is the pen.dev source of truth; the dark instance keeps the same roles (brand
 * gradient and coral accents stay untouched) while flipping the surfaces, ink ramp and status
 * colours. Brushes are precomputed so reading a token never allocates.
 */
@Immutable
class V4Palette(
    val brandStart: Color,
    val brandEnd: Color,
    val brandTint: Color,
    val brandTint2: Color,
    val onBrand: Color,
    val onBrandSoft: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val paper: Color,
    val canvasSoft: Color,
    val roseMuted: Color,
    val teal: Color,
    val tealMid: Color,
    val tealTint: Color,
    val okGreen: Color,
    val okTint: Color,
    val alertBrown: Color,
    val alertBrownSoft: Color,
    val alertTint: Color,
    val lavender: Color,
    val lavenderInk: Color,
    val switchOffTrack: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val heroStroke: Color,
    val heroDivider: Color,
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassStroke: Color,
    val glassDivider: Color,
    val shadowCard: Color,
    val shadowBrand: Color,
    val shadowBrandSoft: Color,
    val shadowHero: Color,
    val shadowTeal: Color,
    val trackSoft: Color,
    val heroTrackSoft: Color,
    val painTrackSoft: Color,
    val cardBorder: Color,
    val quickIconFill: Color,
    val outOfMonthInk: Color,
    val pmsDot: Color,
    val tipsInk: Color,
    val heroGlass: Color,
    val heroTileBorder: Color,
    val heroHinge: Color,
    val heroSheen: Color,
    val isDark: Boolean
) {
    val brandGradient: Brush = Brush.linearGradient(listOf(brandStart, brandEnd))
    val brandGradientHorizontal: Brush = Brush.horizontalGradient(listOf(brandStart, brandEnd))
    val brandGradientVertical: Brush = Brush.verticalGradient(listOf(brandStart, brandEnd))
    val tealGradient: Brush = Brush.linearGradient(listOf(tealMid, teal))
    val heroGradient: Brush = Brush.verticalGradient(listOf(heroTop, heroBottom))
}

val LightV4Palette = V4Palette(
    brandStart = Color(0xFFFF8A71),
    brandEnd = Color(0xFFFF5E7D),
    brandTint = Color(0xFFFFE9EC),
    brandTint2 = Color(0xFFFFD3D9),
    onBrand = Color(0xFFFFFFFF),
    onBrandSoft = Color(0xFFFFF1F2),
    ink = Color(0xFF1B1A1F),
    ink2 = Color(0xFF5B5865),
    ink3 = Color(0xFF8B8894),
    line = Color(0xFFEDEAF0),
    paper = Color(0xFFFFFFFF),
    canvasSoft = Color(0xFFF7F7FA),
    roseMuted = Color(0xFF7A5A64),
    teal = Color(0xFF0E7C7B),
    tealMid = Color(0xFF7FCFC7),
    tealTint = Color(0xFFE1F3F2),
    okGreen = Color(0xFF0F9D6E),
    okTint = Color(0xFFE6F7F0),
    alertBrown = Color(0xFFB4441A),
    alertBrownSoft = Color(0xFF8A3A16),
    alertTint = Color(0xFFFFF0E8),
    lavender = Color(0xFFF1ECFB),
    lavenderInk = Color(0xFF6D5BA6),
    switchOffTrack = Color(0xFFE4E0E8),
    heroTop = Color(0xFFFFF4F5),
    heroBottom = Color(0xFFFFD9DE),
    heroStroke = Color(0xB3FFFFFF),
    heroDivider = Color(0xFFFFD3D9),
    glassFill = Color(0x26FFFFFF),
    glassFillStrong = Color(0x33FFFFFF),
    glassStroke = Color(0x4DFFFFFF),
    glassDivider = Color(0x33FFFFFF),
    shadowCard = Color(0x143D2B33),
    shadowBrand = Color(0x3DFF5E7D),
    shadowBrandSoft = Color(0x33FF5E7D),
    shadowHero = Color(0x1A8A2030),
    shadowTeal = Color(0x3D0E7C7B),
    trackSoft = Color(0xFFF0ECF6),
    heroTrackSoft = Color(0xFFFFE1E5),
    painTrackSoft = Color(0xFFC9E8DC),
    cardBorder = Color(0xFFF1EAED),
    quickIconFill = Color(0xFFFFF1F3),
    outOfMonthInk = Color(0xFFC4C0CC),
    pmsDot = Color(0xFF7C6BB0),
    tipsInk = Color(0xFF7A3B49),
    heroGlass = Color(0xB3FFFFFF),
    heroTileBorder = Color(0xFFFFE1E3),
    heroHinge = Color(0x1B1A1F26),
    heroSheen = Color(0x1AFFFFFF),
    isDark = false
)

/**
 * Dark counterpart. Brand stays brand; surfaces, ink and status tints move to a near-black plum so
 * the coral gradient keeps its pop without the white cards of the light theme.
 */
val DarkV4Palette = V4Palette(
    brandStart = Color(0xFFFF8A71),
    brandEnd = Color(0xFFFF5E7D),
    brandTint = Color(0xFF3A2229),
    brandTint2 = Color(0xFF4A2A33),
    onBrand = Color(0xFFFFFFFF),
    onBrandSoft = Color(0xFFFFDDE3),
    ink = Color(0xFFF4F1F7),
    ink2 = Color(0xFFB5AFBF),
    ink3 = Color(0xFF8A8496),
    line = Color(0xFF2C2733),
    paper = Color(0xFF1A171F),
    canvasSoft = Color(0xFF0F0D13),
    roseMuted = Color(0xFFC99AA6),
    teal = Color(0xFF45C7C0),
    tealMid = Color(0xFF6FD8D1),
    tealTint = Color(0xFF15302E),
    okGreen = Color(0xFF4ADE9B),
    okTint = Color(0xFF12301F),
    alertBrown = Color(0xFFFF9A73),
    alertBrownSoft = Color(0xFFE6936B),
    alertTint = Color(0xFF33201A),
    lavender = Color(0xFF272038),
    lavenderInk = Color(0xFFBBA8F0),
    switchOffTrack = Color(0xFF3C3644),
    heroTop = Color(0xFF2C1B22),
    heroBottom = Color(0xFF211519),
    heroStroke = Color(0x1FFFFFFF),
    heroDivider = Color(0x1AFFFFFF),
    glassFill = Color(0x26FFFFFF),
    glassFillStrong = Color(0x33FFFFFF),
    glassStroke = Color(0x4DFFFFFF),
    glassDivider = Color(0x33FFFFFF),
    shadowCard = Color(0x66000000),
    shadowBrand = Color(0x59FF5E7D),
    shadowBrandSoft = Color(0x4DFF5E7D),
    shadowHero = Color(0x66000000),
    shadowTeal = Color(0x4D0E7C7B),
    trackSoft = Color(0xFF2C2733),
    heroTrackSoft = Color(0xFF4A2A33),
    painTrackSoft = Color(0xFF22503F),
    cardBorder = Color(0xFF2C2733),
    quickIconFill = Color(0xFF3A2229),
    outOfMonthInk = Color(0xFF5A5462),
    pmsDot = Color(0xFFBBA8F0),
    tipsInk = Color(0xFFE9B7A8),
    heroGlass = Color(0x26FFFFFF),
    heroTileBorder = Color(0xFF4A2A33),
    heroHinge = Color(0x59000000),
    heroSheen = Color(0x14FFFFFF),
    isDark = true
)

val LocalV4Palette = staticCompositionLocalOf { LightV4Palette }

/**
 * In-app text size multiplier. Chrome that must keep a fixed physical size (the header actions)
 * divides it back out of the density.
 */
val LocalAppTextScale = staticCompositionLocalOf { 1f }

/** Provides the v4 palette so every design token follows the selected appearance. */
@Composable
fun CycleV4Theme(isDark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalV4Palette provides if (isDark) DarkV4Palette else LightV4Palette,
        content = content
    )
}
