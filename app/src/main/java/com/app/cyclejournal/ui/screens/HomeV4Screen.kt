package com.app.cyclejournal.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.components.RingProgress
import com.app.cyclejournal.ui.components.SoftCard
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.BrandTint
import com.app.cyclejournal.ui.theme.CardBorder
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.HeroBottom
import com.app.cyclejournal.ui.theme.HeroGlass
import com.app.cyclejournal.ui.theme.HeroHinge
import com.app.cyclejournal.ui.theme.HeroSheen
import com.app.cyclejournal.ui.theme.HeroStroke
import com.app.cyclejournal.ui.theme.HeroTileBorder
import com.app.cyclejournal.ui.theme.HeroTop
import com.app.cyclejournal.ui.theme.HeroTrackSoft
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.QuickIconFill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

private const val Masked = "••"

/** Period length the app assumes until the user's own pattern is known. */
private const val CLINICAL_PERIOD_DAYS = 5

private const val HERO_PERIOD = 0
private const val HERO_FERTILE = 1
private const val HERO_OVULATION = 2
private const val HERO_PAGES = 3

private val HeroValueStyle = TextStyle(fontFamily = AppType.Display, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
private val FlipDigitStyle = TextStyle(fontFamily = AppType.Display, fontSize = 44.sp, fontWeight = FontWeight.SemiBold)
private val SummaryValueStyle = TextStyle(fontFamily = AppType.Body, fontSize = 24.sp, fontWeight = FontWeight.Bold)

/** `d MMM` — the compact date form the design uses inside the hero card. */
private fun LocalDate.shortDay(): String =
    format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

/**
 * Home — CycleJournal v4 design (`Beranda · ID v4` / `Home · EN v4`).
 *
 * The hero is a two-page carousel (the design's two pagination dots): page one
 * tracks the period, page two the fertile window. Every value comes from the
 * cycle engine, so nothing on this screen is mocked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeV4Screen(
    isDiscreet: Boolean,
    isPromilMode: Boolean,
    periodDates: Set<LocalDate>,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    cycleStats: CycleStats?,
    todayLog: DailyLogEntity?,
    today: LocalDate = LocalDate.now(),
    onOpenLog: (LocalDate) -> Unit,
    onMarkPeriodEnded: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    // While a period is still running, "days logged so far" is not the pattern length: the design
    // copy reads "<day> of <pattern length>". Use the recorded average once there is one, otherwise
    // the clinical default the rest of the app falls back to.
    val recordedPeriodDays = latestCycle?.periodDurationDays?.takeIf { it > 0 } ?: 0
    val averagePeriodDays = cycleStats?.averagePeriodDuration?.roundToInt()?.takeIf { it > 0 }
    val hasPeriodHistory = averagePeriodDays != null
    val periodLength = averagePeriodDays ?: maxOf(recordedPeriodDays, CLINICAL_PERIOD_DAYS)
    val isBleedingToday = today in periodDates || (latestCycle != null &&
        !today.isBefore(latestCycle.startDate) &&
        today.isBefore(latestCycle.startDate.plusDays(periodLength.toLong())))

    val cycleLength = latestCycle?.cycleLengthDays
        ?: cycleStats?.averageLength?.roundToInt()?.takeIf { it > 0 }
    // With no history yet, the running prediction still implies a cycle length - better than
    // showing "not logged" on the card whose whole job is that number.
    val estimatedLength = rememberedEstimate(latestCycle, fertilePrediction)
    val isEstimated = cycleLength == null && estimatedLength != null
    val shownLength = cycleLength ?: estimatedLength

    // The hero is a real carousel: period day -> fertile window -> ovulation peak. Switching the
    // trying-to-conceive mode on brings the ovulation slide forward, which is what that mode is for.
    val pagerState = rememberPagerState(
        initialPage = if (isPromilMode) HERO_OVULATION else HERO_PERIOD,
        pageCount = { HERO_PAGES }
    )
    LaunchedEffect(isPromilMode) {
        pagerState.animateScrollToPage(if (isPromilMode) HERO_OVULATION else HERO_PERIOD)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 14.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionGap)
    ) {
        // ------------------------------------------------------------ hero --
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("home_hero_pager")
                ) { page ->
                    when (page) {
                        HERO_PERIOD -> PeriodHeroCard(
                            isDiscreet = isDiscreet,
                            isBleeding = isBleedingToday,
                            hasPeriodHistory = hasPeriodHistory,
                            dayInPeriod = dayInPeriod(today, latestCycle, periodLength),
                            periodLength = periodLength,
                            nextPeriodDate = fertilePrediction?.predictedNextPeriodDate,
                            today = today,
                            onAction = { if (isBleedingToday) onMarkPeriodEnded() else onOpenLog(today) }
                        )
                        HERO_FERTILE -> FertileHeroCard(
                            isDiscreet = isDiscreet,
                            fertilePrediction = fertilePrediction,
                            today = today,
                            onAction = onOpenCalendar
                        )
                        else -> OvulationHeroCard(
                            isDiscreet = isDiscreet,
                            isPromilMode = isPromilMode,
                            fertilePrediction = fertilePrediction,
                            today = today,
                            onLogBbt = { onOpenLog(today) },
                            onOpenCalendar = onOpenCalendar
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(HERO_PAGES) { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 20.dp else 7.dp, height = 7.dp)
                                .clip(CircleShape)
                                .background(if (active) BrandEnd else Color(0xFFFFC2CB))
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------- ringkasan --
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard(
                    label = stringResource(
                        if (isEstimated) R.string.v4_home_cycle_estimate else R.string.v4_home_cycle_length
                    ),
                    value = shownLength?.let { stringResource(R.string.v4_home_days_format, it) }
                        ?: stringResource(R.string.v4_home_not_logged),
                    isPlaceholder = shownLength == null,
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp),
                    trailing = {
                        if (shownLength != null) {
                            val progress = (dayInCycle(today, latestCycle).toFloat() / shownLength).coerceIn(0f, 1f)
                            RingProgress(progress = progress, size = 36.dp, trackColor = HeroTrackSoft)
                        }
                    }
                )
                SummaryCard(
                    label = stringResource(
                        if (hasPeriodHistory) R.string.v4_home_period_length else R.string.v4_home_period_length_estimate
                    ),
                    value = if (latestCycle != null || cycleStats != null) {
                        stringResource(R.string.v4_home_days_format, periodLength)
                    } else {
                        stringResource(R.string.v4_home_not_logged)
                    },
                    isPlaceholder = latestCycle == null && cycleStats == null,
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp),
                    trailing = { Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = BrandEnd, modifier = Modifier.size(22.dp)) }
                )
            }
        }

        // ----------------------------------------------------- quick header --
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.v4_home_quick_log),
                    style = AppType.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.v4_home_edit),
                    style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold),
                    color = BrandEnd,
                    modifier = Modifier.clickable { onOpenLog(today) }
                )
            }
        }

        // ------------------------------------------------------- quick view --
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // Four tiles in a row is the design, but at the largest text size the labels no
                // longer fit; the grid drops to 2x2 instead of truncating them.
                val twoColumns = maxWidth < 320.dp
                val cards = listOf(
                    stringResource(R.string.v4_home_card_period) to Icons.Rounded.WaterDrop,
                    stringResource(R.string.v4_home_card_pain) to Icons.Rounded.Healing,
                    stringResource(R.string.v4_home_card_notes) to Icons.Rounded.EditNote,
                    stringResource(R.string.v4_home_card_mucus) to Icons.Rounded.Opacity
                )
                val badgeNotes = hasNote(
                    log = todayLog,
                    symptomsPrefix = stringResource(R.string.daily_notes_symptoms_prefix),
                    notePrefix = stringResource(R.string.daily_notes_note_prefix)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards.chunked(if (twoColumns) 2 else 4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (label, icon) ->
                                QuickCard(
                                    label = label,
                                    icon = icon,
                                    modifier = Modifier.weight(1f),
                                    badge = badgeNotes && label == stringResource(R.string.v4_home_card_notes),
                                    onClick = { onOpenLog(today) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------ suhu basal ---
        item {
            val temperature = todayLog?.basalBodyTempCelsius
            val valueText = if (temperature != null) {
                stringResource(R.string.v4_home_temp_optional, String.format(Locale.US, "%.2f °C", temperature))
            } else {
                stringResource(R.string.v4_home_temp_not_measured)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(Paper)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onOpenLog(today) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.Thermostat, contentDescription = null, tint = Ink3, modifier = Modifier.size(19.dp))
                Text(
                    text = stringResource(R.string.v4_home_basal_temp),
                    style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp),
                    color = Ink2
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = valueText,
                    style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                    color = BrandEnd
                )
            }
        }
    }
}

// --------------------------------------------------------------- hero ------

@Composable
private fun PeriodHeroCard(
    isDiscreet: Boolean,
    isBleeding: Boolean,
    hasPeriodHistory: Boolean,
    dayInPeriod: Int,
    periodLength: Int,
    nextPeriodDate: LocalDate?,
    today: LocalDate,
    onAction: () -> Unit
) {
    val countdown = daysUntil(today, nextPeriodDate)
    val hasPrediction = nextPeriodDate != null
    HeroShell(
        eyebrow = if (isDiscreet) {
            stringResource(R.string.v4_discreet_eyebrow)
        } else {
            stringResource(if (isBleeding) R.string.v4_home_eyebrow_period else R.string.v4_home_eyebrow_next)
        },
        digits = if (!isBleeding && !hasPrediction) "--" else paddedDigits(if (isBleeding) dayInPeriod else countdown),
        big = if (isBleeding) stringResource(R.string.v4_home_of_days, periodLength)
        else if (hasPrediction) stringResource(R.string.v4_home_days_to_go, countdown)
        else stringResource(R.string.v4_home_not_logged),
        sub = if (isBleeding) {
            if (hasPeriodHistory) {
                stringResource(R.string.v4_home_period_pattern, periodLength)
            } else {
                stringResource(R.string.v4_home_period_estimate, periodLength)
            }
        } else {
            nextPeriodDate?.let { stringResource(R.string.v4_home_next_period_sub, it.shortDay()) }.orEmpty()
        },
        actionLabel = stringResource(if (isBleeding) R.string.v4_home_mark_ended else R.string.v4_home_log_period),
        onAction = onAction
    )
}

/**
 * Third hero slide: the ovulation peak. This is the slide the trying-to-conceive mode opens on,
 * and its action deep-links into the log sheet because basal temperature is how the peak gets
 * confirmed in the symptothermal method.
 */
@Composable
private fun OvulationHeroCard(
    isDiscreet: Boolean,
    isPromilMode: Boolean,
    fertilePrediction: FertilePrediction?,
    today: LocalDate,
    onLogBbt: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val ovulationDate = fertilePrediction?.predictedOvulationDate
    val isToday = ovulationDate != null && ovulationDate == today
    val isPast = ovulationDate != null && ovulationDate.isBefore(today)
    val days = daysUntil(today, ovulationDate)
    HeroShell(
        eyebrow = if (isDiscreet) {
            stringResource(R.string.v4_discreet_eyebrow)
        } else {
            stringResource(R.string.v4_home_eyebrow_ovulation)
        },
        digits = paddedDigits(days),
        big = when {
            isToday -> stringResource(R.string.v4_home_ovulation_today)
            isPast -> stringResource(R.string.v4_home_ovulation_passed)
            else -> stringResource(R.string.v4_home_days_to_go, days)
        },
        sub = ovulationDate?.let { stringResource(R.string.v4_home_ovulation_sub, it.shortDay()) }
            ?: stringResource(R.string.v4_home_not_logged),
        actionLabel = stringResource(if (isPromilMode) R.string.v4_cal_log_bbt else R.string.v4_home_open_calendar),
        onAction = if (isPromilMode) onLogBbt else onOpenCalendar
    )
}

@Composable
private fun FertileHeroCard(
    isDiscreet: Boolean,
    fertilePrediction: FertilePrediction?,
    today: LocalDate,
    onAction: () -> Unit
) {
    val days = when {
        fertilePrediction == null -> 0
        today.isBefore(fertilePrediction.fertileWindowStart) ->
            ChronoUnit.DAYS.between(today, fertilePrediction.fertileWindowStart).toInt()
        else -> ChronoUnit.DAYS.between(today, fertilePrediction.fertileWindowEnd).toInt().coerceAtLeast(0)
    }
    HeroShell(
        eyebrow = if (isDiscreet) {
            stringResource(R.string.v4_discreet_eyebrow)
        } else {
            stringResource(R.string.v4_home_eyebrow_fertile)
        },
        digits = paddedDigits(days),
        big = stringResource(R.string.v4_home_fertile_big, days),
        sub = fertilePrediction?.let {
            stringResource(R.string.v4_home_fertile_sub, it.fertileWindowStart.shortDay(), it.fertileWindowEnd.shortDay())
        } ?: stringResource(R.string.v4_home_not_logged),
        actionLabel = stringResource(R.string.v4_home_open_calendar),
        onAction = onAction
    )
}

/** Shared hero shell: pink gradient card (350x~222), flip-clock value, gradient CTA. */
@Composable
private fun HeroShell(
    eyebrow: String,
    digits: String,
    big: String,
    sub: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(30.dp), ambientColor = Color(0x1B1A1F14), spotColor = Color(0x1B1A1F14))
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            .border(1.dp, HeroStroke, RoundedCornerShape(30.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = eyebrow,
                style = AppType.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                color = BrandEnd,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Box(
                // The design fixes this tile at 40dp; `requiredSize` keeps a long eyebrow from
                // squeezing it, which is what made the droplet look smaller.
                modifier = Modifier
                    .requiredSize(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HeroGlass),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = BrandEnd, modifier = Modifier.size(21.dp))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FlipTile(digits.getOrElse(0) { '•' }.toString())
            FlipTile(digits.getOrElse(1) { '•' }.toString())
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(big, style = HeroValueStyle, color = Ink)
                Text(sub, style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp), color = Ink2)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(CircleShape)
                .background(BrandGradient)
                .clickable(onClick = onAction),
            contentAlignment = Alignment.Center
        ) {
            Text(actionLabel, style = AppType.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = OnBrand)
        }
    }
}

/** One flip-clock tile of the hero value (design: 60x76, 44sp display digit, hinge + sheen). */
@Composable
private fun FlipTile(digit: String) {
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 76.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Paper)
            .border(1.dp, HeroTileBorder, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = digit, style = FlipDigitStyle, color = Ink)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.Center)
                .background(HeroHinge)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .background(HeroSheen)
        )
    }
}

// ------------------------------------------------------------ ringkasan ----

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isPlaceholder: Boolean = false,
    trailing: @Composable () -> Unit
) {
    SoftCard(modifier = modifier, fill = Paper, border = CardBorder, shadow = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp),
                color = Ink3
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // A missing value is a label, not a number: the big display style would clip it.
                Text(
                    text = value,
                    style = if (isPlaceholder) {
                        AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    } else {
                        SummaryValueStyle
                    },
                    color = if (isPlaceholder) Ink3 else Ink,
                    maxLines = if (isPlaceholder) 2 else 1,
                    modifier = Modifier.weight(1f)
                )
                trailing()
            }
        }
    }
}

// ----------------------------------------------------------- quick view ----

@Composable
private fun QuickCard(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badge: Boolean = false,
    onClick: () -> Unit
) {
    SoftCard(
        modifier = modifier.height(96.dp),
        fill = Paper,
        border = CardBorder,
        radius = 20.dp,
        shadow = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(QuickIconFill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = BrandEnd, modifier = Modifier.size(19.dp))
                }
                Text(
                    text = label,
                    style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal),
                    color = Ink2,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
            // The design's badge slot now reports real state: this day already has a note.
            if (badge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(BrandEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = OnBrand, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

/**
 * True when the day carries a free-text note. Notes are stored behind the localized symptom
 * prefix when the same day also has symptom chips, so that segment is stripped first.
 */
private fun hasNote(log: DailyLogEntity?, symptomsPrefix: String, notePrefix: String): Boolean {
    val raw = log?.notes ?: return false
    val body = if (raw.contains(notePrefix)) raw.substringAfter(notePrefix).trim() else raw
    return body.isNotBlank() && !body.startsWith(symptomsPrefix)
}

// --------------------------------------------------------------- helpers ---

private fun paddedDigits(value: Int): String =
    String.format(Locale.US, "%02d", value.coerceIn(0, 99))

/** Cycle length implied by the running prediction, until a full cycle has been recorded. */
private fun rememberedEstimate(latestCycle: CycleEntity?, prediction: FertilePrediction?): Int? {
    if (latestCycle?.cycleLengthDays != null || latestCycle == null || prediction == null) return null
    val days = ChronoUnit.DAYS.between(latestCycle.startDate, prediction.predictedNextPeriodDate).toInt()
    return days.takeIf { it in 15..60 }
}

private fun daysUntil(today: LocalDate, date: LocalDate?): Int =
    if (date == null) 0 else ChronoUnit.DAYS.between(today, date).toInt().coerceAtLeast(0)

private fun dayInPeriod(today: LocalDate, latestCycle: CycleEntity?, periodLength: Int): Int {
    if (latestCycle == null) return 1
    if (today.isBefore(latestCycle.startDate)) return 0
    return (ChronoUnit.DAYS.between(latestCycle.startDate, today) + 1).toInt().coerceIn(1, periodLength)
}

private fun dayInCycle(today: LocalDate, latestCycle: CycleEntity?): Int =
    if (latestCycle == null) 1 else (ChronoUnit.DAYS.between(latestCycle.startDate, today) + 1).toInt().coerceAtLeast(1)
