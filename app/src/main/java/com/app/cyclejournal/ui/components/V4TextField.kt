package com.app.cyclejournal.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.ui.theme.AlertBrown
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.FieldLine
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Paper

/**
 * The app's only text input.
 *
 * Material3's default outline is close to invisible on the tinted dialog surfaces, so the border,
 * container and label colours are pinned here instead of being inherited from the theme.
 */
@Composable
fun V4TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { text -> { Text(text, fontSize = 13.sp) } },
        placeholder = placeholder?.let { text -> { Text(text, fontSize = 13.sp, color = Ink3) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        textStyle = AppType.body.copy(fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            errorTextColor = Ink,
            focusedContainerColor = Paper,
            unfocusedContainerColor = Paper,
            errorContainerColor = Paper,
            cursorColor = BrandEnd,
            errorCursorColor = AlertBrown,
            focusedBorderColor = BrandEnd,
            unfocusedBorderColor = FieldLine,
            errorBorderColor = AlertBrown,
            focusedLabelColor = BrandEnd,
            unfocusedLabelColor = Ink2,
            errorLabelColor = AlertBrown
        )
    )
}
