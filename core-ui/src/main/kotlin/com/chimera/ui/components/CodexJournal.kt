package com.chimera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
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
import com.chimera.ui.theme.ChimeraElevation
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.Cinzel
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.ParchmentLight
import com.chimera.ui.theme.Vellum

data class CodexEntry(
    val title: String,
    val category: String,
    val body: String,
    val isRead: Boolean = true
)

/**
 * A codex/journal entry viewer with illuminated initial heading,
 * category label, and parchment-styled body text.
 */
@Composable
fun CodexJournalEntry(
    entry: CodexEntry,
    modifier: Modifier = Modifier,
    cardFillColor: Color = CodexJournalDefaults.cardFillColor,
    cardBorderColor: Color = CodexJournalDefaults.cardBorderColor,
    cardElevation: Dp = CodexJournalDefaults.cardElevation,
    titleStyle: TextStyle = CodexJournalDefaults.titleStyle,
    categoryStyle: TextStyle = CodexJournalDefaults.categoryStyle,
    bodyStyle: TextStyle = CodexJournalDefaults.bodyStyle
) {
    ManuscriptCard(
        modifier = modifier,
        illuminatedText = entry.title,
        fillColor = cardFillColor,
        borderColor = cardBorderColor,
        elevation = cardElevation
    ) {
        // Category label
        Text(
            text = entry.category.uppercase(),
            style = categoryStyle
        )

        Spacer(modifier = Modifier.height(ChimeraSpacing.tiny))

        // Title with illuminated initial
        Text(
            text = entry.title,
            style = titleStyle
        )

        Spacer(modifier = Modifier.height(ChimeraSpacing.micro))

        // Decorative divider
        Divider(
            color = AgedGold.copy(alpha = 0.4f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(ChimeraSpacing.small))

        // Body text
        Text(
            text = entry.body,
            style = bodyStyle,
            color = if (entry.isRead) Vellum else FadedBone
        )
    }
}

/**
 * A list of codex entries displayed as a scrollable journal.
 */
@Composable
fun CodexJournal(
    entries: List<CodexEntry>,
    modifier: Modifier = Modifier,
    onEntryClick: ((CodexEntry) -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ChimeraSpacing.medium)
    ) {
        entries.forEach { entry ->
            CodexJournalEntry(entry = entry)
        }
    }
}

object CodexJournalDefaults {
    val cardFillColor = ParchmentLight
    val cardBorderColor = Oxblood
    val cardElevation = ChimeraElevation.low
    val titleStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = AgedGold
    )
    val categoryStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        color = Oxblood
    )
    val bodyStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Vellum
    )
}