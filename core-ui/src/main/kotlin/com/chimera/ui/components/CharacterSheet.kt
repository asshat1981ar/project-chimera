package com.chimera.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

/**
 * Character sheet display showing name, title, and stat bars
 * in a manuscript card with illuminated initial.
 */
@Composable
fun CharacterSheet(
    name: String,
    title: String,
    stats: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    cardFillColor: Color = CharacterSheetDefaults.cardFillColor,
    cardBorderColor: Color = CharacterSheetDefaults.cardBorderColor,
    cardElevation: Dp = CharacterSheetDefaults.cardElevation,
    statBarHeight: Dp = CharacterSheetDefaults.statBarHeight,
    nameStyle: TextStyle = CharacterSheetDefaults.nameStyle,
    titleStyle: TextStyle = CharacterSheetDefaults.titleStyle,
    statLabelStyle: TextStyle = CharacterSheetDefaults.statLabelStyle,
    onStatClick: ((String) -> Unit)? = null
) {
    ManuscriptCard(
        modifier = modifier,
        illuminatedText = name,
        fillColor = cardFillColor,
        borderColor = cardBorderColor,
        elevation = cardElevation
    ) {
        Text(
            text = name,
            style = nameStyle
        )
        Text(
            text = title,
            style = titleStyle,
            modifier = Modifier.padding(bottom = ChimeraSpacing.small)
        )

        stats.forEach { (label, fraction) ->
            ManuscriptStatBar(
                fraction = fraction,
                label = label,
                height = statBarHeight,
                labelStyle = statLabelStyle
            )
            Spacer(modifier = Modifier.height(ChimeraSpacing.micro))
        }
    }
}

object CharacterSheetDefaults {
    val cardFillColor = ParchmentLight
    val cardBorderColor = Oxblood
    val cardElevation = ChimeraElevation.medium
    val statBarHeight = 10.dp
    val nameStyle = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = AgedGold
    )
    val titleStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = FadedBone
    )
    val statLabelStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = Vellum
    )
}