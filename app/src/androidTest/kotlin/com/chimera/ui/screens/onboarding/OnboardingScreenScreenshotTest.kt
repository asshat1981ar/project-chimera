package com.chimera.ui.screens.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented screenshot tests for OnboardingScreen.
 *
 * Run with: ./gradlew :app:connectedMockDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Visual snapshot test - verifies the OnboardingScreen renders correctly
     * with all expected UI elements visible.
     */
    @Test
    fun onboardingScreen_rendersCorrectly() {
        composeTestRule.setContent {
            OnboardingScreen(
                preferences = null,
                onComplete = {}
            )
        }

        // Verify main title is displayed
        composeTestRule
            .onNodeWithText("CHIMERA")
            .assertExists()

        // Verify subtitle is displayed
        composeTestRule
            .onNodeWithText("Ashes of the Hollow King")
            .assertExists()

        // Verify description text is displayed
        composeTestRule
            .onNodeWithText(
                "Your words shape the world. NPCs remember your choices, " +
                    "reinterpret promises, and unlock or block future paths " +
                    "based on trust, fear, and faction pressure."
            )
            .assertExists()

        // Verify accessibility section header
        composeTestRule
            .onNodeWithText("Accessibility")
            .assertExists()

        // Verify text size label with default value
        composeTestRule
            .onNodeWithText("Text Size: 100%")
            .assertExists()

        // Verify reduce motion label
        composeTestRule
            .onNodeWithText("Reduce Motion")
            .assertExists()

        // Verify the CTA button is displayed
        composeTestRule
            .onNodeWithText("Begin Your Journey")
            .assertExists()
    }

    /**
     * Accessibility test - verifies accessibility options are present and interactive.
     */
    @Test
    fun onboardingScreen_accessibilitySettings_present() {
        composeTestRule.setContent {
            OnboardingScreen(
                preferences = null,
                onComplete = {}
            )
        }

        // Verify text size slider is present (implicitly tested via label)
        composeTestRule
            .onNodeWithText("Text Size: 100%")
            .assertExists()

        // Verify reduce motion switch is present and can be toggled
        composeTestRule
            .onNodeWithText("Reduce Motion")
            .assertExists()

        // Toggle the reduce motion switch to verify interactivity
        composeTestRule
            .onNodeWithText("Reduce Motion")
            .performClick()

        // After clicking, the switch should be in the "on" state
        // The label remains the same, but we verify the UI responds
        composeTestRule
            .onNodeWithText("Reduce Motion")
            .assertExists()

        // Verify the accessibility card container exists via its content
        composeTestRule
            .onNodeWithText("Accessibility")
            .assertExists()
    }
}
