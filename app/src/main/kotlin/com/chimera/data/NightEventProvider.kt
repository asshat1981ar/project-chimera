package com.chimera.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class NightEvent(
    val id: String,
    val title: String,
    val narrative: String,
    val choices: List<NightEventChoice> = emptyList()
)

data class NightEventChoice(
    val text: String,
    val moraleDelta: Float = 0f,
    val dispositionDelta: Map<String, Float> = emptyMap(),
    val outcome: String = ""
)

@Singleton
class NightEventProvider @Inject constructor() {

    private val events = listOf(
        NightEvent(
            id = "campfire_stories",
            title = "Campfire Stories",
            narrative = "The fire crackles low. A companion offers to share a tale from before the Hollow fell.",
            choices = listOf(
                NightEventChoice("Listen quietly", moraleDelta = 0.05f, outcome = "The story stirs something in the group. Morale rises."),
                NightEventChoice("Share your own story", moraleDelta = 0.08f, outcome = "Your tale of the road draws the party closer together."),
                NightEventChoice("Keep watch instead", moraleDelta = -0.02f, outcome = "You stay alert. The night passes uneventfully.")
            )
        ),
        NightEvent(
            id = "strange_noise",
            title = "Strange Noise",
            narrative = "A sound echoes from beyond the firelight. Something moves in the shadows.",
            choices = listOf(
                NightEventChoice("Investigate alone", moraleDelta = -0.05f, outcome = "You find nothing but old bones. The tension lingers."),
                NightEventChoice("Send a companion", moraleDelta = 0f, outcome = "They return shaken but unharmed. Just the wind."),
                NightEventChoice("Ignore it", moraleDelta = -0.03f, outcome = "The sound fades. Nobody sleeps well.")
            )
        ),
        NightEvent(
            id = "companion_argument",
            title = "Heated Words",
            narrative = "Two companions exchange sharp words by the dying embers. The tension is palpable.",
            choices = listOf(
                NightEventChoice("Mediate the dispute", moraleDelta = 0.05f, outcome = "You broker an uneasy peace. Both parties nod grudgingly."),
                NightEventChoice("Let them sort it out", moraleDelta = -0.08f, outcome = "The argument escalates before burning out. The silence afterwards is heavy."),
                NightEventChoice("Side with one party", moraleDelta = -0.03f, outcome = "One companion looks grateful. The other turns cold.")
            )
        ),
        NightEvent(
            id = "clear_skies",
            title = "Clear Skies",
            narrative = "For the first time in days, the sky above the Hollow is clear. Stars shine through the haze.",
            choices = listOf(
                NightEventChoice("Rest well", moraleDelta = 0.1f, outcome = "Everyone sleeps deeply. Morning comes with renewed purpose."),
                NightEventChoice("Use the light to forage", moraleDelta = 0.03f, outcome = "You find useful herbs nearby. A small but welcome gain.")
            )
        )
    )

    fun getRandomEvent(morale: Float): NightEvent {
        // Weight negative events higher when morale is low
        return if (morale < 0.3f && Random.nextFloat() < 0.6f) {
            events.filter { it.id == "strange_noise" || it.id == "companion_argument" }.random()
        } else {
            events.random()
        }
    }
}
