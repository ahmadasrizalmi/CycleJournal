@file:OptIn(ExperimentalTextApi::class)

package com.app.cyclejournal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R

/**
 * CycleJournal v4 design tokens.
 *
 * The names mirror the pen.dev document (`Beranda · ID v4`, `Kalender · ID v13/v14/v15`,
 * `Analisis · ID v5/v6`, `Setelan · ID v4`). Values live in [V4Palette] so the same design renders
 * in the light and dark appearance; read them from a composable - the active palette comes from
 * [CycleV4Theme].
 */

// ---------------------------------------------------------------- Brand -----
val BrandStart: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandStart
val BrandEnd: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandEnd
val BrandTint: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandTint
val BrandTint2: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandTint2

/** 215° brand gradient used by the header band, primary buttons and the FAB. */
val BrandGradient: Brush @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandGradient
val BrandGradientHorizontal: Brush @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandGradientHorizontal
val BrandGradientVertical: Brush @Composable @ReadOnlyComposable get() = LocalV4Palette.current.brandGradientVertical

// -------------------------------------------------------------- Neutrals ----
val Ink: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.ink
val Ink2: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.ink2
val Ink3: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.ink3
val Line: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.line

/** `$surface` — cards and sheets. */
val Paper: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.paper

/** `$canvas-soft` — page background behind the cards. */
val CanvasSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.canvasSoft
val OnBrand: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.onBrand
val OnBrandSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.onBrandSoft

/** Muted plum used for secondary text on the soft (hero) pink surface. */
val RoseMuted: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.roseMuted

// --------------------------------------------------------------- Accents ----
val Teal: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.teal
val TealMid: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.tealMid
val TealTint: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.tealTint
val TealGradient: Brush @Composable @ReadOnlyComposable get() = LocalV4Palette.current.tealGradient

val OkGreen: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.okGreen
val OkTint: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.okTint

val AlertBrown: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.alertBrown
val AlertBrownSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.alertBrownSoft
val AlertTint: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.alertTint

val Lavender: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.lavender
val LavenderInk: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.lavenderInk

val SwitchOffTrack: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.switchOffTrack

// ------------------------------------------------------ Hero / glass fills --
/** Soft hero gradient (`#FFF4F5 → #FFD9DE`) used by the Home hero and the profile card. */
val HeroTop: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroTop
val HeroBottom: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroBottom
val HeroGradient: Brush @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroGradient
val HeroStroke: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroStroke
val HeroDivider: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroDivider

/** Translucent white blocks that sit on top of the brand band. */
val GlassFill: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.glassFill
val GlassFillStrong: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.glassFillStrong
val GlassStroke: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.glassStroke
val GlassDivider: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.glassDivider

// ------------------------------------------------------------ Shared parts --
/** Rail behind timelines and the "today" caret in charts. */
val TrackSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.trackSoft

/** Ring track behind the cycle-progress ring and the flip-clock border. */
val HeroTrackSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroTrackSoft

/** Track behind the pain slider. */
val PainTrackSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.painTrackSoft

/** Warm hairline the summary and quick-log cards use instead of [Line]. */
val CardBorder: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.cardBorder

val QuickIconFill: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.quickIconFill
val OutOfMonthInk: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.outOfMonthInk
val PmsDot: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.pmsDot
val TipsInk: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.tipsInk

/** Translucent white icon tile that sits on the hero gradient. */
val HeroGlass: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroGlass
val HeroTileBorder: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroTileBorder
val HeroHinge: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroHinge
val HeroSheen: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.heroSheen

// --------------------------------------------------------------- Shadows ----
val ShadowCard: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.shadowCard
val ShadowBrand: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.shadowBrand
val ShadowBrandSoft: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.shadowBrandSoft
val ShadowHero: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.shadowHero
val ShadowTeal: Color @Composable @ReadOnlyComposable get() = LocalV4Palette.current.shadowTeal

// ------------------------------------------------------------ Fixed sizing --
object Dimens {
    val ScreenPadding = 20.dp
    val CardRadius = 22.dp
    val CardRadiusLg = 26.dp
    val SheetRadius = 28.dp
    val PillRadius = 999.dp
    val SectionGap = 10.dp
    val BandHeight = 125.dp
    val BottomBarHeight = 92.dp
    val TabBarHeight = 62.dp
}

/**
 * Type ramp from the design.
 *
 * Figtree carries the UI, Inter the body copy and Literata the display/wordmark. All three ship as
 * variable fonts (weight axis), so each weight is a variation of one file instead of a static set.
 */
private val Figtree = FontFamily(
    Font(R.font.figtree, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.figtree, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.figtree, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.figtree, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private val Inter = FontFamily(
    Font(R.font.inter, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private val Literata = FontFamily(
    Font(R.font.literata, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.literata, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.literata, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.literata, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

object AppType {
    /** `$font-display` — Literata: hero value, wordmark, splash title. */
    val Display = Literata

    /** `$font-heading` — Plus Jakarta Sans: page titles, card/section titles in the log sheet. */
    val Heading = PlusJakartaSans

    /** `$font-ui` — Figtree: eyebrows, buttons, nav, list rows. */
    val Ui = Figtree

    /** `$font-body` — Inter: body copy, summary values and table numerals. */
    val Body = Inter

    val pageTitle = TextStyle(fontFamily = Heading, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val heroValue = TextStyle(fontFamily = Heading, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    val cardTitle = TextStyle(fontFamily = Ui, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    val rowTitle = TextStyle(fontFamily = Ui, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    val listRow = TextStyle(fontFamily = Body, fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val listValue = TextStyle(fontFamily = Body, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontFamily = Body, fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontFamily = Body, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    val micro = TextStyle(fontFamily = Body, fontSize = 10.sp, fontWeight = FontWeight.Normal)
    val label = TextStyle(fontFamily = Ui, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
    val tabLabel = TextStyle(fontFamily = Ui, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val button = TextStyle(fontFamily = Ui, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}
