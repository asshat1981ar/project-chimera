package com.chimera.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson

/**
 * Compact HUD card for the current active quest objective.
 *
 * Displays the objective title, a one-line story context, and an action rune.
 * Callers map domain models to the primitive parameters so this component stays
 * usable across features without depending on domain types.
 *
 * @param title Short objective title (e.g. "Speak with the Warden").
 * @param storyContext One-line narrative context (e.g. "He waits at the Hollow Gate").
 * @param actionHint Optional hint shown below the title (e.g. "Tap to continue scene").
 * @param action Rune indicating the primary action the player can take.
 * @param onClick Invoked when the card is tapped.
 */
@Composable
fun QuestObjectiveHudCard(
    title: String,
    storyContext: String,
    actionHint: String? = null,
    action: ObjectiveHudAction = ObjectiveHudAction.NONE,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ManuscriptCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick),
        fillColor = MaterialTheme.colorScheme.surface,
        borderColor = EmberGold.copy(alpha = 0.4f),
        borderWidth = 1.dp,
        contentPadding = 16.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ObjectiveStatusRune(action = action)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = storyContext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FadedBone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                actionHint?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = DimAsh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
