package com.app.cyclejournal.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.theme.*
import java.time.LocalDate

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val step by viewModel.currentStep.collectAsState()

    var pinSetup by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var cycleLength by remember { mutableStateOf(28f) }
    var periodDuration by remember { mutableStateOf(5f) }
    var lastPeriodDate by remember { mutableStateOf(LocalDate.now().minusDays(14)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            OnboardingStep.DISCLAIMER -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_pdf_header),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPink
                    )
                    Text(
                        text = stringResource(R.string.disclaimer_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.disclaimer_body),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.nextStep(OnboardingStep.PIN_SETUP) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
                    ) {
                        Text(stringResource(R.string.action_agree_continue), fontWeight = FontWeight.Bold)
                    }
                }
            }

            OnboardingStep.PIN_SETUP -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(48.dp))
                    Text(stringResource(R.string.setup_pin_title), style = MaterialTheme.typography.titleLarge)

                    // 4-digit Pin Dots
                    PinDotsView(pinLength = pinSetup.length, isError = false)

                    // Numeric Numpad
                    NumericKeypad(onKeyPress = { key ->
                        if (key == "DEL") {
                            if (pinSetup.isNotEmpty()) pinSetup = pinSetup.dropLast(1)
                        } else if (pinSetup.length < 4) {
                            pinSetup += key
                            if (pinSetup.length == 4) {
                                viewModel.tempPin = pinSetup
                                viewModel.nextStep(OnboardingStep.PIN_CONFIRM)
                            }
                        }
                    })
                }
            }

            OnboardingStep.PIN_CONFIRM -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(48.dp))
                    Text(stringResource(R.string.confirm_pin_title), style = MaterialTheme.typography.titleLarge)

                    PinDotsView(pinLength = pinConfirm.length, isError = pinError)

                    if (pinError) {
                        Text(stringResource(R.string.pin_mismatch), color = AlertText, fontSize = 13.sp)
                    }

                    NumericKeypad(onKeyPress = { key ->
                        if (key == "DEL") {
                            if (pinConfirm.isNotEmpty()) pinConfirm = pinConfirm.dropLast(1)
                            pinError = false
                        } else if (pinConfirm.length < 4) {
                            pinConfirm += key
                            if (pinConfirm.length == 4) {
                                if (pinConfirm == viewModel.tempPin) {
                                    viewModel.savePin(pinConfirm)
                                    viewModel.nextStep(OnboardingStep.BASELINE_CYCLE)
                                } else {
                                    pinError = true
                                    pinConfirm = ""
                                }
                            }
                        }
                    })
                }
            }

            OnboardingStep.BASELINE_CYCLE -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.baseline_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Average Cycle Length Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.baseline_cycle_length))
                            Text("${cycleLength.toInt()} Hari", fontWeight = FontWeight.Bold, color = PrimaryPink)
                        }
                        Slider(
                            value = cycleLength,
                            onValueChange = { cycleLength = it },
                            valueRange = 21f..45f,
                            steps = 23
                        )
                    }

                    // Average Period Duration Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.baseline_period_duration))
                            Text("${periodDuration.toInt()} Hari", fontWeight = FontWeight.Bold, color = PrimaryPink)
                        }
                        Slider(
                            value = periodDuration,
                            onValueChange = { periodDuration = it },
                            valueRange = 2f..10f,
                            steps = 7
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.finalizeOnboarding(
                                lastPeriodDate = lastPeriodDate,
                                averageCycleLength = cycleLength.toInt(),
                                averagePeriodDuration = periodDuration.toInt(),
                                onComplete = onFinished
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
                    ) {
                        Text(stringResource(R.string.action_finish_onboarding), fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
fun PinDotsView(pinLength: Int, isError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        for (i in 0 until 4) {
            val isFilled = i < pinLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isError -> AlertBorder
                            isFilled -> PrimaryPink
                            else -> Color(0xFFE2E8F0)
                        }
                    )
            )
        }
    }
}

@Composable
fun NumericKeypad(onKeyPress: (String) -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in keys) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                for (key in row) {
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(68.dp))
                    } else {
                        Button(
                            onClick = { onKeyPress(key) },
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8FAFC), contentColor = TextPrimary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                        ) {
                            Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
