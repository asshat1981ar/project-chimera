package com.chimera.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * ParchmentInputField: Parchment-styled text input with gold focus
 * indicator and rubricated label.
 */
@Composable
fun ParchmentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Oxblood,
                modifier = Modifier.padding(bottom = ChimeraSpacing.micro)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = if (placeholder != null) {
                { Text(placeholder, color = FadedBone.copy(alpha = 0.5f)) }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Vellum,
                unfocusedTextColor = Vellum,
                focusedBorderColor = AgedGold,
                unfocusedBorderColor = Oxblood.copy(alpha = 0.4f),
                focusedContainerColor = Iron.copy(alpha = 0.6f),
                unfocusedContainerColor = Iron.copy(alpha = 0.3f),
                cursorColor = AgedGold,
                focusedLabelColor = AgedGold,
                unfocusedLabelColor = FadedBone
            ),
            shape = RoundedCornerShape(ChimeraCorners.small)
        )
    }
}