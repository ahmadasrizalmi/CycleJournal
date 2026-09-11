package com.app.cyclejournal.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.theme.AlertText
import com.app.cyclejournal.ui.theme.BackgroundWhite
import com.app.cyclejournal.ui.theme.PrimaryPink
import com.app.cyclejournal.ui.theme.TextPrimary

@Composable
fun PinLockScreen(
    onPinEntered: (String) -> Boolean,
    onBiometricRequested: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = PrimaryPink,
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = stringResource(R.string.security_pin_lock_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            PinDotsView(pinLength = pin.length, isError = isError)

            if (isError) {
                Text(stringResource(R.string.security_pin_incorrect), color = AlertText, fontSize = 13.sp)
            }

            NumericKeypad(onKeyPress = { key ->
                if (key == "DEL") {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    isError = false
                } else if (pin.length < 4) {
                    pin += key
                    if (pin.length == 4) {
                        val success = onPinEntered(pin)
                        if (!success) {
                            isError = true
                            pin = ""
                        }
                    }
                }
            })

            // Biometric Shortcut Button
            IconButton(
                onClick = onBiometricRequested,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF8FAFC), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Fingerprint,
                    contentDescription = stringResource(R.string.security_fingerprint_content_desc),
                    tint = PrimaryPink,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
