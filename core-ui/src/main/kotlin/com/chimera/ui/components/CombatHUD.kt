package com.chimera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.Cinzel
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum
import com.chimera.ui.theme.Verdigris

/**
 * Combat heads-up display overlay. Shows health, mana, and stamina bars
 * with labels, plus an action row for combat buttons.
 * Uses ManuscriptStatBar for each bar and GothicOutlinedButton for actions.
 */
@Composable
fun CombatHUD(
    healthFraction: Float,
    manaFraction: Float,
    staminaFraction: Float,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    actions: List<String> = CombatHUDDefaults.actions,
    healthBarFillColor: Color = CombatHUDDefaults.healthBarFillColor,
    manaBarFillColor: Color = CombatHUDDefaults.manaBarFillColor,
    staminaBarFillColor: Color = CombatHUDDefaults.staminaBarFillColor,
    trackColor: Color = CombatHUDDefaults.trackColor,
    barHeight: Dp = CombatHUDDefaults.barHeight,
    labelStyle: TextStyle = CombatHUDDefaults.labelStyle
) {
    Column(
        modifier = modifier.padding(ChimeraSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(ChimeraSpacing.small)
    ) {
        // Health bar
        ManuscriptStatBar(
            fraction = healthFraction,
            label = "HP",
            fillColor = healthBarFillColor,
            trackColor = trackColor,
            height = barHeight,
            labelStyle = labelStyle
        )

        // Mana bar
        ManuscriptStatBar(
            fraction = manaFraction,
            label = "MP",
            fillColor = manaBarFillColor,
            trackColor = trackColor,
            height = barHeight,
            labelStyle = labelStyle
        )

        // Stamina bar
        ManuscriptStatBar(
            fraction = staminaFraction,
            label = "ST",
            fillColor = staminaBarFillColor,
            trackColor = trackColor,
            height = barHeight,
            labelStyle = labelStyle
        )

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChimeraSpacing.small)
        ) {
            actions.forEach { action ->
                GothicOutlinedButton(
                    onClick = { onActionClick(action) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = action)
                }
            }
        }
    }
}

object CombatHUDDefaults {
    val healthBarFillColor = Oxblood
    val manaBarFillColor = Verdigris
    val staminaBarFillColor = AgedGold
    val trackColor = Iron
    val barHeight = 10.dp
    val actions = listOf("Attack", "Defend", "Magic", "Flee")
    val labelStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = Vellum
    )
}