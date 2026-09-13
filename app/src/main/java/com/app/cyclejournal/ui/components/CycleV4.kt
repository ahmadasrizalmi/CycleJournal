package com.app.cyclejournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.theme.*

/**
 * Shared building blocks for the CycleJournal v4 design system.
 * Every value mirrors the pen.dev document tokens (see DesignTokens.kt).
 */

// --------------------------------------------------------------- Header -----

/** Full-bleed brand gradient band with the logo, wordmark and quick action buttons. */
@Composable
fun BrandBand(
    languageCode: String,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onDiscreetToggle: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "CycleJournal",
    topPadding: Dp = 18.dp,
    bottomPadding: Dp = 14.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(BrandGradient)
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(top = topPadding, bottom = bottomPadding)
    ) {
        // The header keeps its own physical size: growing the quick actions and the wordmark with the
        // in-app text size only crowded the app name out at the largest step.
        val scale = LocalAppTextScale.current.coerceAtLeast(0.1f)
        val baseDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density / scale, baseDensity.fontScale)
        ) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The transparent brand mark sits on a translucent circle - the same treatment as the
                // action buttons - because a bare coral logo would sink into the coral band.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassFillStrong),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.v4_logo_mark),
                        contentDescription = stringResource(R.string.header_logo_content_desc),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = AppType.pageTitle.copy(fontSize = 18.sp),
                    color = OnBrand,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                GlassIconButton(
                    icon = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = stringResource(
                        if (isDarkMode) R.string.v4_theme_switch_to_light else R.string.v4_theme_switch_to_dark
                    ),
                    onClick = onThemeToggle
                )
                Spacer(Modifier.width(8.dp))
                LanguagePill(languageCode, onLanguageClick)
                Spacer(Modifier.width(8.dp))
                GlassIconButton(Icons.Rounded.Visibility, "Discreet mode", onDiscreetToggle)
            }
        }
    }
}

@Composable
fun GlassIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassFillStrong)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = OnBrand, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun LanguagePill(code: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(OnBrand)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // A bare code with a chevron did not read as "change language"; the globe does.
        Icon(
            Icons.Rounded.Language,
            contentDescription = stringResource(R.string.v4_language_change),
            tint = BrandEnd,
            modifier = Modifier.size(17.dp)
        )
        Text(code, color = BrandEnd, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------- Cards -----

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    fill: Color = Paper,
    border: Color = Line,
    borderWidth: Dp = 1.dp,
    radius: Dp = Dimens.CardRadius,
    shadow: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (shadow) Modifier.shadow(14.dp, RoundedCornerShape(radius), ambientColor = ShadowCard, spotColor = ShadowCard) else Modifier)
            .clip(RoundedCornerShape(radius))
            .background(fill)
            .border(borderWidth, border, RoundedCornerShape(radius)),
        content = content
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppType.label,
        color = Ink3,
        modifier = modifier
    )
}

// ---------------------------------------------------------------- Chips -----

@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    fontSize: Int = 14,
    horizontalPadding: Dp = 10.dp,
    selectedFill: Color = BrandTint,
    selectedBorder: Color = BrandEnd,
    selectedText: Color = BrandEnd,
    unselectedFill: Color = Paper,
    unselectedBorder: Color = Line,
    unselectedText: Color = Ink2
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(if (selected) selectedFill else unselectedFill)
            .border(1.dp, if (selected) selectedBorder else unselectedBorder, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedText else unselectedText,
            maxLines = 1
        )
    }
}

/** Equal-width single-select row (replaces the old iOS style segmented control). */
@Composable
fun SegmentedPills(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    fontSize: Int = 14,
    horizontalPadding: Dp = 10.dp
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            PillChip(
                text = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
                height = height,
                fontSize = fontSize,
                horizontalPadding = horizontalPadding
            )
        }
    }
}

// --------------------------------------------------------------- Switch -----

@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(if (checked) BrandEnd else SwitchOffTrack)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (checked) 21.dp else 3.dp, y = 3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ---------------------------------------------------------------- Tiles -----

@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Dp = 34.dp,
    corner: Dp = 11.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Setting tile used by the Settings screen quick grid. */
@Composable
fun SettingTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconTile(icon = icon, tint = tint, background = Color(0x99FFFFFF), size = 30.dp, corner = 10.dp, iconSize = 16.dp)
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Text(title, style = AppType.rowTitle, color = Ink, maxLines = 1)
        Text(subtitle, style = AppType.micro, color = Ink3, maxLines = 2)
    }
}

// ---------------------------------------------------------------- Chart -----

@Composable
fun RingProgress(
    progress: Float,
    size: Dp = 68.dp,
    trackColor: Color = HeroTrackSoft,
    color: Color = BrandEnd,
    strokeWidth: Dp = 7.dp,
    label: String? = null,
    labelColor: Color = BrandEnd
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        if (label != null) {
            Text(label, style = AppType.rowTitle, color = labelColor, textAlign = TextAlign.Center)
        }
    }
}

/** Column chart used by the "Cycle length" card and the biphasic temperature chart. */
@Composable
fun MiniBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 70.dp,
    gap: Dp = 10.dp,
    barColor: Color = BrandTint,
    highlightBrush: Brush = BrandGradientVertical,
    highlightLast: Boolean = true,
    showValues: Boolean = true
) {
    val max = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { index, value ->
                val isHighlight = highlightLast && index == values.lastIndex
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showValues) {
                        Text(
                            text = value.toInt().toString(),
                            style = AppType.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = Ink2
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((barHeight * (value / max)).coerceAtLeast(6.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isHighlight) Modifier.background(highlightBrush)
                                else Modifier.background(barColor)
                            )
                    )
                }
            }
        }
        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = AppType.caption,
                        color = Ink3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * One row of the cycle timeline: a rounded track with the period and fertile segments,
 * an ovulation marker and the "today" caret.
 */
@Composable
fun CycleTimelineRow(
    label: String,
    rightLabel: String,
    cycleLengthDays: Int,
    periodDays: Int,
    fertileStartDay: Int,
    fertileEndDay: Int,
    ovulationDay: Int,
    todayDay: Int?,
    modifier: Modifier = Modifier,
    onLabelClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = AppType.body.copy(fontWeight = FontWeight.Bold), color = Ink)
            if (onLabelClick != null) {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Ink3,
                    modifier = Modifier.size(14.dp).clickable(onClick = onLabelClick)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(rightLabel, style = AppType.caption.copy(fontWeight = FontWeight.SemiBold), color = Ink2)
        }
        // DrawScope is not a composable scope, so the palette is read once here.
        val railColor = TrackSoft
        val periodBrush = BrandGradientHorizontal
        val fertileFill = TealTint
        val fertileStroke = TealMid
        val ovulationColor = Teal
        val caretColor = Ink
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            val length = cycleLengthDays.coerceAtLeast(1)
            val trackH = 6.dp.toPx()
            val segH = 10.dp.toPx()
            val centerY = size.height / 2f
            fun xOf(day: Float) = (size.width * (day / length)).coerceIn(0f, size.width)
            val radius = trackH / 2f

            // track
            drawRoundRect(
                color = railColor,
                topLeft = Offset(0f, centerY - trackH / 2f),
                size = Size(size.width, trackH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
            )
            // period segment
            val periodEnd = xOf(periodDays.toFloat())
            drawRoundRect(
                brush = periodBrush,
                topLeft = Offset(0f, centerY - segH / 2f),
                size = Size(periodEnd.coerceAtLeast(segH), segH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(segH / 2f, segH / 2f)
            )
            // fertile window
            val fStart = xOf(fertileStartDay.toFloat())
            val fEnd = xOf(fertileEndDay.toFloat())
            val fWidth = (fEnd - fStart).coerceAtLeast(segH * 2)
            drawRoundRect(
                color = fertileFill,
                topLeft = Offset(fStart, centerY - segH / 2f),
                size = Size(fWidth, segH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(segH / 2f, segH / 2f)
            )
            drawRoundRect(
                color = fertileStroke,
                topLeft = Offset(fStart, centerY - segH / 2f),
                size = Size(fWidth, segH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(segH / 2f, segH / 2f),
                style = Stroke(width = 2f)
            )
            // ovulation marker
            drawCircle(color = ovulationColor, radius = 7.dp.toPx() / 2f + 1f, center = Offset(xOf(ovulationDay.toFloat()), centerY))
            drawCircle(color = Color.White, radius = 7.dp.toPx() / 2f, center = Offset(xOf(ovulationDay.toFloat()), centerY))
            // today caret
            if (todayDay != null) {
                val x = xOf(todayDay.toFloat())
                drawRoundRect(
                    color = caretColor,
                    topLeft = Offset(x - 1.dp.toPx(), 0f),
                    size = Size(2.dp.toPx(), size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }
    }
}

// --------------------------------------------------------------- Buttons ----

@Composable
fun PrimaryPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 48.dp,
    brush: Brush = BrandGradient,
    shadowColor: Color = ShadowBrandSoft
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(brush)
            .shadow(10.dp, CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = OnBrand, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AppType.button, color = OnBrand, maxLines = 1)
    }
}

@Composable
fun SoftPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 48.dp,
    fill: Color = CanvasSoft,
    contentColor: Color = Ink2
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(fill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AppType.button, color = contentColor, maxLines = 1)
    }
}

@Composable
fun HeroCtaRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Brush = BrandGradient
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .background(fill)
            .shadow(14.dp, RoundedCornerShape(Dimens.CardRadius), ambientColor = ShadowBrandSoft, spotColor = ShadowBrandSoft)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(GlassFillStrong),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = OnBrand, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppType.cardTitle, color = OnBrand)
            Text(subtitle, style = AppType.micro.copy(fontWeight = FontWeight.Medium), color = OnBrandSoft)
        }
        Box(
            modifier = Modifier
                .height(34.dp)
                .clip(CircleShape)
                .background(OnBrand)
                .clickable(onClick = onAction)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(actionLabel, style = AppType.caption.copy(fontWeight = FontWeight.Bold), color = BrandEnd)
        }
    }
}

// ------------------------------------------------------------- List rows ----

@Composable
fun SettingsListRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueColor: Color = Ink2,
    icon: ImageVector? = null,
    iconTint: Color = Ink3,
    titleColor: Color = Ink,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(
            title,
            style = AppType.listRow,
            color = titleColor,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(value, style = AppType.listValue, color = valueColor)
        }
        trailing?.invoke()
        if (showChevron) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink3, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun HairLine(color: Color = Line, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

// ------------------------------------------------------------ Bottom bar ----

/** Resource ids the marketing screenshot flows select the navigation by. */
private val NAV_TAGS = listOf("nav_home", "nav_calendar", "nav_report", "nav_settings")

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    labels: List<String> = listOf("Home", "Calendar", "Analysis", "Settings")
) {
    // Same bar geometry as 1.1.3: 64dp white bar with the add button floating over its top edge.
    val icons = listOf(Icons.Filled.Home, Icons.Filled.DateRange, Icons.Filled.Description, Icons.Filled.Settings)
    Box(modifier = modifier.fillMaxWidth()) {
        // The bar paints `paper` and then pads itself by the system navigation inset, so the area
        // behind the gesture bar shares the bar colour instead of showing the page background.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HairLine(color = Line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab(labels[0], icons[0], selectedIndex == 0, NAV_TAGS[0]) { onSelect(0) }
                NavTab(labels[1], icons[1], selectedIndex == 1, NAV_TAGS[1]) { onSelect(1) }

                // Space reserved for the floating add button.
                Spacer(Modifier.width(54.dp))

                NavTab(labels[2], icons[2], selectedIndex == 2, NAV_TAGS[2]) { onSelect(2) }
                NavTab(labels[3], icons[3], selectedIndex == 3, NAV_TAGS[3]) { onSelect(3) }
            }
        }

        // Elevated central add button, lifted above the bar's top edge.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(54.dp)
                .shadow(12.dp, CircleShape, ambientColor = ShadowBrand, spotColor = ShadowBrand)
                .clip(CircleShape)
                .background(BrandGradient)
                .testTag("fab_log")
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = labels.getOrNull(0),
                tint = OnBrand,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val tint = if (selected) BrandEnd else Ink3
    Column(
        modifier = Modifier
            .testTag(tag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint,
            maxLines = 1
        )
    }
}

