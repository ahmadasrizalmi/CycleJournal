package com.app.cyclejournal.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.ui.theme.AlertBrown
import com.app.cyclejournal.ui.theme.AlertTint
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.BrandTint
import com.app.cyclejournal.ui.theme.CanvasSoft
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OkGreen
import com.app.cyclejournal.ui.theme.OkTint
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.PainTrackSoft
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.ShadowBrandSoft
import com.app.cyclejournal.ui.theme.Teal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SheetTitleStyle = TextStyle(fontFamily = AppType.Heading, fontSize = 19.sp, fontWeight = FontWeight.Bold)
private val SectionTitleStyle = TextStyle(fontFamily = AppType.Heading, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
private val SectionValueStyle = TextStyle(fontFamily = AppType.Heading, fontSize = 18.sp, fontWeight = FontWeight.Bold)

/** Custom symptom chips are user-authored and persisted across sessions. */
private const val CUSTOM_SYMPTOMS_PREFS = "custom_symptoms_store"
private const val CUSTOM_SYMPTOMS_KEY = "custom_symptoms"

/**
 * Daily log sheet — CycleJournal v4 design (`Kalender · ID v14 (log sheet)` and
 * `v15 (detail klinis terbuka)`): flow segmented row, pain slider, collapsible
 * clinical detail (basal temperature · mucus · medication · notes) and body
 * symptom chips.
 *
 * Storage contract is unchanged from the legacy sheet so clinical exports and the
 * cycle engine keep reading the same fields: symptoms land in `painLocation` and
 * are mirrored into `notes` behind the localized symptom prefix.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LogSheetV4(
    targetDate: LocalDate,
    initialLog: DailyLogEntity?,
    onDismiss: () -> Unit,
    onSave: (DailyLogEntity) -> Unit
) {
    val context = LocalContext.current

    var selectedFlow by remember(initialLog) { mutableStateOf(initialLog?.flow ?: FlowIntensity.NONE) }
    var vasScore by remember(initialLog) { mutableStateOf((initialLog?.painVasScore ?: 0).toFloat()) }
    var selectedMucus by remember(initialLog) { mutableStateOf(initialLog?.cervicalMucus ?: CervicalMucusType.NONE) }
    var hasTakenAnalgesic by remember(initialLog) { mutableStateOf(initialLog?.takenAnalgesic ?: false) }
    var bbtValue by remember(initialLog) { mutableStateOf(initialLog?.basalBodyTempCelsius) }

    val customPrefs = remember { context.getSharedPreferences(CUSTOM_SYMPTOMS_PREFS, Context.MODE_PRIVATE) }
    val customSymptoms = remember {
        mutableStateListOf<String>().apply {
            addAll(customPrefs.getStringSet(CUSTOM_SYMPTOMS_KEY, emptySet()).orEmpty())
        }
    }

    val chipLabels = listOf(
        stringResource(R.string.v4_log_symptom_cramps),
        stringResource(R.string.v4_log_symptom_back),
        stringResource(R.string.v4_log_symptom_breasts)
    )
    // Records written before the v4 sheet stored the longer legacy labels; accept both so an
    // existing log opens with its symptoms still selected.
    val legacyLabels = listOf(
        stringResource(R.string.daily_symptom_pelvic_cramps),
        stringResource(R.string.daily_symptom_lower_back_pain),
        stringResource(R.string.daily_symptom_tender_breasts)
    )
    val selectedSymptoms = remember(initialLog) {
        mutableStateListOf<String>().apply {
            val stored = initialLog?.painLocation?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
            stored.forEach { value ->
                val chipIndex = legacyLabels.indexOfFirst { it.equals(value, ignoreCase = true) }
                add(if (chipIndex >= 0) chipLabels[chipIndex] else value)
            }
        }
    }

    val symptomsPrefix = stringResource(R.string.daily_notes_symptoms_prefix)
    val notePrefix = stringResource(R.string.daily_notes_note_prefix)
    val storedNotes = remember(initialLog) {
        val raw = initialLog?.notes ?: ""
        when {
            raw.contains(notePrefix) -> raw.substringAfter(notePrefix).trim()
            raw.startsWith(symptomsPrefix) -> ""
            else -> raw
        }
    }
    var notesInput by remember(initialLog) { mutableStateOf(storedNotes) }

    val detailCount = listOf(
        selectedSymptoms.isNotEmpty(),
        bbtValue != null,
        selectedMucus != CervicalMucusType.NONE,
        hasTakenAnalgesic,
        notesInput.isNotBlank()
    ).count { it }
    // A day without clinical detail stays two taps; the section only opens by itself when the
    // stored log for this date already carries detail.
    var isDetailExpanded by remember(initialLog) { mutableStateOf(initialLog != null && detailCount > 0) }

    var isAddSymptomOpen by remember { mutableStateOf(false) }
    var newSymptomInput by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isDirty = selectedFlow != (initialLog?.flow ?: FlowIntensity.NONE) ||
        vasScore.toInt() != (initialLog?.painVasScore ?: 0) ||
        selectedMucus != (initialLog?.cervicalMucus ?: CervicalMucusType.NONE) ||
        hasTakenAnalgesic != (initialLog?.takenAnalgesic ?: false) ||
        bbtValue != initialLog?.basalBodyTempCelsius ||
        selectedSymptoms.toList() != (initialLog?.painLocation?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()) ||
        notesInput.trim() != storedNotes

    val requestDismiss: () -> Unit = { if (isDirty) showDiscardDialog = true else onDismiss() }

    val isPeriodFlow = selectedFlow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
    val formattedDate = remember(targetDate) {
        targetDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Paper,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = Dimens.SheetRadius, topEnd = Dimens.SheetRadius)
    ) {
        // The sheet's own height budget shrinks as the text size grows, so the scrollable body takes
        // whatever is left after the header and the pinned footer - otherwise the save button gets
        // squeezed by the incoming constraints.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val bodyMaxHeight = (maxHeight - 210.dp).coerceAtLeast(240.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTagsAsResourceId = true }
                .testTag("sheet_daily_log")
        ) {
            // ------------------------------------------------------- handle --
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Line)
                )
            }

            // ------------------------------------------------------- header --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.v4_log_title),
                        style = SheetTitleStyle,
                        color = Ink
                    )
                    Text(text = formattedDate, style = AppType.caption.copy(fontSize = 13.sp), color = Ink3)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CanvasSoft)
                        .clickable(onClick = requestDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.app_close), tint = Ink2, modifier = Modifier.size(17.dp))
                }
            }

            // --------------------------------------------------------- body --
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = bodyMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --------------------------------------------------- flow ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.v4_log_flow),
                            style = SectionTitleStyle,
                            color = Ink
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(if (isPeriodFlow) R.string.v4_log_flow_hint_active else R.string.v4_log_flow_hint_none),
                            style = AppType.caption.copy(fontSize = 14.sp),
                            color = Ink3
                        )
                    }
                    val flowLabels = listOf(
                        stringResource(R.string.v4_log_flow_none),
                        stringResource(R.string.v4_log_flow_spotting),
                        stringResource(R.string.v4_log_flow_light),
                        stringResource(R.string.v4_log_flow_medium),
                        stringResource(R.string.v4_log_flow_heavy)
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val selectedIndex = FLOW_ORDER.indexOf(selectedFlow)
                        // Five pills fit one row at normal size; at the largest text size they
                        // move to 3 + 2 rather than truncating "Spotting" and "Medium".
                        if (maxWidth >= 320.dp) {
                            SegmentedPills(
                                options = flowLabels,
                                selectedIndex = selectedIndex.coerceAtLeast(0),
                                onSelect = { selectedFlow = FLOW_ORDER[it] },
                                height = 40.dp,
                                fontSize = 12,
                                horizontalPadding = 4.dp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SegmentedPills(
                                    options = flowLabels.subList(0, 3),
                                    selectedIndex = if (selectedIndex in 0..2) selectedIndex else -1,
                                    onSelect = { selectedFlow = FLOW_ORDER[it] },
                                    height = 40.dp,
                                    fontSize = 12,
                                    horizontalPadding = 4.dp
                                )
                                SegmentedPills(
                                    options = flowLabels.subList(3, 5),
                                    selectedIndex = if (selectedIndex >= 3) selectedIndex - 3 else -1,
                                    onSelect = { selectedFlow = FLOW_ORDER[it + 3] },
                                    height = 40.dp,
                                    fontSize = 12,
                                    horizontalPadding = 4.dp
                                )
                            }
                        }
                    }
                }

                // --------------------------------------------------- pain ---
                val painLabelRes = when {
                    vasScore.toInt() == 0 -> R.string.v4_log_pain_free
                    vasScore <= 3f -> R.string.v4_log_pain_mild
                    vasScore <= 6f -> R.string.v4_log_pain_moderate
                    else -> R.string.v4_log_pain_severe
                }
                val painColor = if (vasScore.toInt() == 0) OkGreen else AlertBrown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkTint)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.v4_log_pain),
                            style = SectionTitleStyle,
                            color = Ink
                        )
                        Text(
                            text = stringResource(R.string.v4_log_pain_score, vasScore.toInt()),
                            style = SectionValueStyle,
                            color = Ink
                        )
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(Paper)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(painLabelRes), style = AppType.caption.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = painColor)
                        }
                    }
                    Slider(
                        value = vasScore,
                        onValueChange = { vasScore = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Paper,
                            activeTrackColor = OkGreen,
                            inactiveTrackColor = PainTrackSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_pain_slider")
                    )
                }

                // ----------------------------------------- clinical detail ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Line, RoundedCornerShape(20.dp))
                        .background(Paper)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sheet_detail_toggle")
                            .clickable { isDetailExpanded = !isDetailExpanded }
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(if (isDetailExpanded) R.string.v4_log_clinical_hide else R.string.v4_log_clinical),
                                style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                color = Ink
                            )
                            Text(stringResource(R.string.v4_log_clinical_sub), style = AppType.caption, color = Ink3)
                        }
                        Icon(
                            imageVector = if (isDetailExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isDetailExpanded) {
                        HairLine()
                        // ------------------------------------------- basal temp --
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val stacked = maxWidth < 300.dp
                            val stepper: @Composable () -> Unit = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StepperButton(Icons.Rounded.Remove, enabled = bbtValue != null) {
                                        bbtValue = (((bbtValue ?: BBT_DEFAULT) - 0.01).coerceIn(BBT_MIN, BBT_MAX))
                                    }
                                    Text(
                                        text = bbtValue?.let { String.format(Locale.US, "%.2f °C", it) } ?: "—",
                                        style = SectionValueStyle,
                                        color = if (bbtValue == null) Ink3 else Ink,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.testTag("log_bbt_value")
                                    )
                                    StepperButton(Icons.Rounded.Add, enabled = true, filled = true) {
                                        bbtValue = (((bbtValue ?: BBT_DEFAULT) + 0.01).coerceIn(BBT_MIN, BBT_MAX))
                                    }
                                }
                            }
                            if (stacked) {
                                // At the largest text size the label and the stepper stop sharing a row.
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(stringResource(R.string.v4_log_bbt), style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp), color = Ink)
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { stepper() }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(stringResource(R.string.v4_log_bbt), style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp), color = Ink)
                                    Spacer(Modifier.weight(1f))
                                    stepper()
                                }
                            }
                        }
                        HairLine()
                        // --------------------------------------------- analgesic --
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.v4_log_meds), style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp), color = Ink)
                            Spacer(Modifier.weight(1f))
                            AppSwitch(checked = hasTakenAnalgesic, onCheckedChange = { hasTakenAnalgesic = it })
                        }
                        HairLine()
                        // ------------------------------------------------ mucus --
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, top = 14.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.v4_log_mucus),
                                    style = SectionTitleStyle,
                                    color = Ink
                                )
                                Spacer(Modifier.weight(1f))
                                Text(stringResource(R.string.v4_log_mucus_hint), style = AppType.caption.copy(fontSize = 14.sp), color = Ink3)
                            }
                            val mucusLabels = MUCUS_ORDER.map {
                                stringResource(
                                    when (it) {
                                        CervicalMucusType.NONE -> R.string.v4_log_mucus_none
                                        CervicalMucusType.DRY -> R.string.v4_log_mucus_dry
                                        CervicalMucusType.STICKY -> R.string.v4_log_mucus_sticky
                                        CervicalMucusType.CREAMY -> R.string.v4_log_mucus_creamy
                                        CervicalMucusType.WATERY -> R.string.v4_log_mucus_watery
                                        CervicalMucusType.EGG_WHITE -> R.string.v4_log_mucus_egg
                                    }
                                )
                            }
                            SegmentedPills(
                                options = mucusLabels.subList(0, 3),
                                selectedIndex = MUCUS_ORDER.indexOf(selectedMucus).let { if (it in 0..2) it else -1 },
                                onSelect = { selectedMucus = MUCUS_ORDER[it] },
                                height = 40.dp
                            )
                            SegmentedPills(
                                options = mucusLabels.subList(3, 6),
                                selectedIndex = MUCUS_ORDER.indexOf(selectedMucus).let { if (it >= 3) it - 3 else -1 },
                                onSelect = { selectedMucus = MUCUS_ORDER[it + 3] },
                                height = 40.dp
                            )
                        }
                        HairLine()
                        // ------------------------------------------------- notes --
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.v4_log_notes),
                                style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                color = Ink
                            )
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                placeholder = { Text(stringResource(R.string.v4_log_notes_hint), fontSize = 13.sp, color = Ink3) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("log_notes"),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2,
                                textStyle = AppType.body.copy(fontSize = 14.sp)
                            )
                        }
                    }
                }

                // ------------------------------------------------ symptoms ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.v4_log_symptoms),
                            style = SectionTitleStyle,
                            color = Ink
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.v4_log_add_symptom),
                            style = AppType.cardTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = BrandEnd,
                            modifier = Modifier.clickable { isAddSymptomOpen = true }
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val allChips = chipLabels + customSymptoms
                        allChips.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { label ->
                                    PillChip(
                                        text = label,
                                        selected = selectedSymptoms.contains(label),
                                        onClick = {
                                            if (selectedSymptoms.contains(label)) selectedSymptoms.remove(label)
                                            else selectedSymptoms.add(label)
                                        },
                                        modifier = Modifier.height(40.dp),
                                        fontSize = 14
                                    )
                                }
                                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------------- footer --
            HairLine()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 34.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(18.dp, CircleShape, ambientColor = ShadowBrandSoft, spotColor = ShadowBrandSoft)
                        .clip(CircleShape)
                        .background(BrandGradient)
                        .testTag("sheet_save")
                        .clickable {
                            onSave(
                                DailyLogEntity(
                                    date = targetDate,
                                    flow = selectedFlow,
                                    basalBodyTempCelsius = bbtValue,
                                    cervicalMucus = selectedMucus,
                                    painVasScore = vasScore.toInt(),
                                    painLocation = selectedSymptoms.takeIf { it.isNotEmpty() }?.joinToString(", "),
                                    takenAnalgesic = hasTakenAnalgesic,
                                    notes = buildString {
                                        if (selectedSymptoms.isNotEmpty()) append(symptomsPrefix + selectedSymptoms.joinToString(", "))
                                        if (notesInput.isNotBlank()) {
                                            if (isNotEmpty()) append(notePrefix)
                                            append(notesInput.trim())
                                        }
                                    }.takeIf { it.isNotBlank() }
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.v4_log_save),
                        style = AppType.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                        color = OnBrand
                    )
                }
            }
        }
        }

        if (isAddSymptomOpen) {
            AlertDialog(
                onDismissRequest = { isAddSymptomOpen = false },
                title = { Text(stringResource(R.string.daily_custom_symptom_title), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(stringResource(R.string.daily_custom_symptom_prompt), fontSize = 14.sp, color = Ink2)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSymptomInput,
                            onValueChange = { newSymptomInput = it },
                            placeholder = { Text(stringResource(R.string.daily_custom_symptom_hint), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val trimmed = newSymptomInput.trim()
                        if (trimmed.isNotEmpty()) {
                            if (!customSymptoms.contains(trimmed)) {
                                customSymptoms.add(trimmed)
                                customPrefs.edit()
                                    .putStringSet(CUSTOM_SYMPTOMS_KEY, customSymptoms.toSet())
                                    .apply()
                            }
                            if (!selectedSymptoms.contains(trimmed)) selectedSymptoms.add(trimmed)
                        }
                        newSymptomInput = ""
                        isAddSymptomOpen = false
                    }) { Text(stringResource(R.string.daily_save), fontWeight = FontWeight.Bold, color = BrandEnd) }
                },
                dismissButton = {
                    TextButton(onClick = { isAddSymptomOpen = false }) { Text(stringResource(R.string.settings_cancel)) }
                }
            )
        }

        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text(stringResource(R.string.daily_discard_title), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.daily_discard_message), fontSize = 14.sp, color = Ink2) },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscardDialog = false
                        onDismiss()
                    }) { Text(stringResource(R.string.daily_discard_confirm), fontWeight = FontWeight.Bold, color = AlertBrown) }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) { Text(stringResource(R.string.daily_discard_keep)) }
                }
            )
        }
    }
}

// ---------------------------------------------------------------- pieces ----

@Composable
private fun StepperButton(
    icon: ImageVector,
    enabled: Boolean,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (filled) BrandTint else CanvasSoft)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> Ink3
                filled -> BrandEnd
                else -> Ink2
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

private val FLOW_ORDER = listOf(
    FlowIntensity.NONE,
    FlowIntensity.SPOTTING,
    FlowIntensity.LIGHT,
    FlowIntensity.MEDIUM,
    FlowIntensity.HEAVY
)

private val MUCUS_ORDER = listOf(
    CervicalMucusType.NONE,
    CervicalMucusType.DRY,
    CervicalMucusType.STICKY,
    CervicalMucusType.CREAMY,
    CervicalMucusType.WATERY,
    CervicalMucusType.EGG_WHITE
)

private const val BBT_DEFAULT = 36.50
private const val BBT_MIN = 34.00
private const val BBT_MAX = 42.00
