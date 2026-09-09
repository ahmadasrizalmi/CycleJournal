package com.app.cyclejournal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailyLogInputSheet(
    selectedDate: LocalDate,
    initialLog: DailyLogEntity? = null,
    onSaveLog: (DailyLogEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var flow by remember(initialLog) { mutableStateOf(initialLog?.flow ?: FlowIntensity.NONE) }
    var vasScore by remember(initialLog) { mutableStateOf(initialLog?.painVasScore?.toFloat() ?: 0f) }
    var bbtInput by remember(initialLog) {
        mutableStateOf(initialLog?.basalBodyTempCelsius?.toString() ?: "")
    }
    var mucus by remember(initialLog) { mutableStateOf(initialLog?.cervicalMucus ?: CervicalMucusType.NONE) }
    var analgesicTaken by remember(initialLog) { mutableStateOf(initialLog?.takenAnalgesic ?: false) }
    var notes by remember(initialLog) { mutableStateOf(initialLog?.notes ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.title_daily_log),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.getDefault())),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Tutup")
            }
        }

        HorizontalDivider(color = BorderSubtle)

        // 1. Flow Intensity Chips (with Vector WaterDrop)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.header_flow),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val flowOptions = listOf(
                    FlowIntensity.NONE to (stringResource(R.string.flow_none) to Color(0xFFF1F5F9)),
                    FlowIntensity.SPOTTING to (stringResource(R.string.flow_spotting) to Color(0xFFFFE4E6)),
                    FlowIntensity.LIGHT to (stringResource(R.string.flow_light) to Color(0xFFFDA4AF)),
                    FlowIntensity.MEDIUM to (stringResource(R.string.flow_medium) to Color(0xFFF43F5E)),
                    FlowIntensity.HEAVY to (stringResource(R.string.flow_heavy) to Color(0xFFBE123C))
                )

                flowOptions.forEach { (intensity, data) ->
                    val (label, tint) = data
                    val isSelected = flow == intensity
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) tint else Color(0xFFF8FAFC))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryPink else BorderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { flow = intensity }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                tint = if (isSelected && intensity in listOf(FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) Color.White else PrimaryPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected && intensity in listOf(FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 2. Clinical VAS Pain Scale (0-10 Slider without emoji)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.header_pain_vas),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${vasScore.toInt()} / 10",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (vasScore >= 7) AlertBorder else PrimaryPink
                )
            }

            Slider(
                value = vasScore,
                onValueChange = { vasScore = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = if (vasScore >= 7) AlertBorder else PrimaryPink,
                    activeTrackColor = if (vasScore >= 7) AlertBorder else PrimaryPink,
                    inactiveTrackColor = Color(0xFFF1F5F9)
                )
            )

            val vasDescription = when (vasScore.toInt()) {
                0 -> stringResource(R.string.vas_desc_none)
                in 1..3 -> stringResource(R.string.vas_desc_mild)
                in 4..6 -> stringResource(R.string.vas_desc_moderate)
                else -> stringResource(R.string.vas_desc_severe)
            }

            Text(
                text = vasDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = if (vasScore >= 7) AlertText else TextSecondary
            )

            // Red Flag Dynamic Card
            AnimatedVisibility(visible = vasScore >= 7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlertBg)
                        .border(1.dp, AlertBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = AlertBorder
                    )
                    Text(
                        text = stringResource(R.string.alert_severe_pain),
                        fontSize = 12.sp,
                        color = AlertText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. Basal Body Temperature & Analgesics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = bbtInput,
                onValueChange = { bbtInput = it },
                label = { Text(stringResource(R.string.header_bbt)) },
                placeholder = { Text(stringResource(R.string.bbt_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Thermostat, contentDescription = null, tint = PrimaryPink)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { analgesicTaken = !analgesicTaken }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Checkbox(
                    checked = analgesicTaken,
                    onCheckedChange = { analgesicTaken = it }
                )
                Column {
                    Icon(
                        imageVector = Icons.Outlined.Medication,
                        contentDescription = null,
                        tint = PrimaryCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Analgesik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. Cervical Mucus Chips (Symptothermal)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.header_cervical_mucus),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val mucusOptions = listOf(
                    CervicalMucusType.NONE to stringResource(R.string.mucus_dry),
                    CervicalMucusType.STICKY to stringResource(R.string.mucus_sticky),
                    CervicalMucusType.CREAMY to stringResource(R.string.mucus_creamy),
                    CervicalMucusType.EGG_WHITE to stringResource(R.string.mucus_egg_white)
                )

                mucusOptions.forEach { (type, label) ->
                    val isSelected = mucus == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FertileBg else Color(0xFFF8FAFC))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) FertileText else BorderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { mucus = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Opacity,
                                contentDescription = null,
                                tint = if (isSelected) FertileText else PrimaryCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FertileText else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 5. Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.header_notes)) },
            placeholder = { Text(stringResource(R.string.notes_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        // 6. Save Button (Coral-to-Pink Gradient)
        Button(
            onClick = {
                val bbtDouble = bbtInput.toDoubleOrNull()
                val log = DailyLogEntity(
                    date = selectedDate,
                    flow = flow,
                    basalBodyTempCelsius = bbtDouble,
                    cervicalMucus = mucus,
                    painVasScore = vasScore.toInt(),
                    takenAnalgesic = analgesicTaken,
                    notes = notes.takeIf { it.isNotBlank() }
                )
                onSaveLog(log)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
                Text(
                    text = stringResource(R.string.action_save_log),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
