package com.chimera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.chimera.ui.theme.ChimeraElevation
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

data class CraftingIngredient(
    val name: String,
    val quantityRequired: Int,
    val quantityAvailable: Int
)

data class CraftingRecipe(
    val name: String,
    val ingredients: List<CraftingIngredient>,
    val description: String = "",
    val progressFraction: Float = 0f,
    val canCraft: Boolean = false
)

/**
 * A crafting station UI showing a recipe in a ManuscriptCard with
 * ingredient list, progress bar, and craft button.
 */
@Composable
fun CraftingStation(
    recipe: CraftingRecipe,
    onCraftClick: () -> Unit,
    modifier: Modifier = Modifier,
    progressFillColor: Color = CraftingStationDefaults.progressFillColor,
    progressTrackColor: Color = CraftingStationDefaults.progressTrackColor,
    cardElevation: Dp = CraftingStationDefaults.cardElevation,
    recipeNameStyle: TextStyle = CraftingStationDefaults.recipeNameStyle,
    ingredientStyle: TextStyle = CraftingStationDefaults.ingredientStyle
) {
    ManuscriptCard(
        modifier = modifier,
        illuminatedText = recipe.name,
        elevation = cardElevation
    ) {
        // Recipe name
        Text(
            text = recipe.name,
            style = recipeNameStyle
        )

        if (recipe.description.isNotEmpty()) {
            Text(
                text = recipe.description,
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = FadedBone
                ),
                modifier = Modifier.padding(top = ChimeraSpacing.micro, bottom = ChimeraSpacing.small)
            )
        }

        Spacer(modifier = Modifier.height(ChimeraSpacing.small))

        // Ingredients list
        recipe.ingredients.forEach { ingredient ->
            val hasEnough = ingredient.quantityAvailable >= ingredient.quantityRequired
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ingredient.name,
                    style = ingredientStyle,
                    color = if (hasEnough) Vellum else Oxblood
                )
                Text(
                    text = "${ingredient.quantityAvailable}/${ingredient.quantityRequired}",
                    style = ingredientStyle,
                    color = if (hasEnough) AgedGold else Oxblood
                )
            }
            Spacer(modifier = Modifier.height(ChimeraSpacing.micro))
        }

        Spacer(modifier = Modifier.height(ChimeraSpacing.small))

        // Progress bar
        ManuscriptStatBar(
            fraction = recipe.progressFraction,
            label = "Progress",
            fillColor = progressFillColor,
            trackColor = progressTrackColor
        )

        Spacer(modifier = Modifier.height(ChimeraSpacing.small))

        // Craft button
        GothicButton(
            onClick = onCraftClick,
            enabled = recipe.canCraft
        ) {
            Text(text = if (recipe.canCraft) "Craft" else "Missing Materials")
        }
    }
}

object CraftingStationDefaults {
    val progressFillColor = AgedGold
    val progressTrackColor = Iron
    val cardElevation = ChimeraElevation.low
    val recipeNameStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = Vellum
    )
    val ingredientStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = FadedBone
    )
}