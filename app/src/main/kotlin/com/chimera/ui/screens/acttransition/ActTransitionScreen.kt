package com.chimera.ui.screens.acttransition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.components.GothicOutlinedButton
import com.chimera.ui.theme.AbyssBlack
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.MutedEmber
import com.chimera.ui.util.ChapterDisplayStrings
import kotlinx.coroutines.delay

/** Full-screen act-transition interstitial. Fades in, shows act title + flavour quote, then
 *  waits for the player to tap Continue before navigating to HOME. */
@Composable
fun ActTransitionScreen(
    actTag: String,
    onContinue: () -> Unit
) {
    val actLabel  = ChapterDisplayStrings.tagToActLabel(actTag)
    val actTitle  = ChapterDisplayStrings.tagToTitle(actTag)
    val quote     = ChapterDisplayStrings.tagToQuote(actTag)
    val quoteSrc  = ChapterDisplayStrings.tagToQuoteSource(actTag)

    // Stagger content fade-in for cinematic feel
    var showLabel   by remember { mutableStateOf(false) }
    var showTitle   by remember { mutableStateOf(false) }
    var showQuote   by remember { mutableStateOf(false) }
    var showButton  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300);  showLabel  = true
        delay(500);  showTitle  = true
        delay(600);  showQuote  = true
        delay(800);  showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ChimeraSpacing.xxl)
        ) {

            // Act number label — small caps style, letter-spaced
            AnimatedVisibility(
                visible = showLabel,
                enter = fadeIn(tween(800)),
                exit = fadeOut()
            ) {
                Text(
                    text = actLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = EmberGold
                )
            }

            Spacer(Modifier.height(ChimeraSpacing.medium))

            // Divider above title
            AnimatedVisibility(visible = showTitle, enter = fadeIn(tween(600))) {
                Divider(
                    modifier = Modifier.width(56.dp),
                    color = MutedEmber,
                    thickness = 1.dp
                )
            }

            Spacer(Modifier.height(ChimeraSpacing.medium))

            // Main act title
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(900)),
                exit = fadeOut()
            ) {
                Text(
                    text = actTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(14.dp))

            // Divider below title
            AnimatedVisibility(visible = showTitle, enter = fadeIn(tween(600))) {
                Divider(
                    modifier = Modifier.width(56.dp),
                    color = MutedEmber,
                    thickness = 1.dp
                )
            }

            Spacer(Modifier.height(ChimeraSpacing.xl))

            // Flavour quote
            AnimatedVisibility(
                visible = showQuote,
                enter = fadeIn(tween(1000)),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = quote,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        color = FadedBone,
                        textAlign = TextAlign.Center
                    )
                    if (quoteSrc.isNotBlank()) {
                        Spacer(Modifier.height(ChimeraSpacing.small))
                        Text(
                            text = "— $quoteSrc",
                            style = MaterialTheme.typography.labelSmall,
                            color = DimAsh,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(ChimeraSpacing.xxl))

            // Continue button
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(600)),
                exit = fadeOut()
            ) {
                GothicOutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
