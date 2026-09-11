package com.app.cyclejournal.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.ui.theme.AlertBorder
import com.app.cyclejournal.ui.theme.PrimaryPink
import com.app.cyclejournal.ui.theme.TextPrimary

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
