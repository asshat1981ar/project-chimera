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
        ),
        NightEvent(
            id = "wounded_stranger",
            title = "The Wounded Stranger",
            narrative = "A figure stumbles out of the darkness, bleeding from a wound that refuses to close. They collapse near the fire.",
            choices = listOf(
                NightEventChoice("Tend their wounds", moraleDelta = 0.06f, outcome = "The stranger's breathing steadies. By morning, they vanish -- leaving behind a small token of thanks."),
                NightEventChoice("Question them first", moraleDelta = 0f, outcome = "They mutter about shadows with teeth before passing out. You learn nothing useful."),
                NightEventChoice("Drive them away", moraleDelta = -0.06f, outcome = "The stranger staggers back into the dark. Their cries echo for a long time.")
            )
        ),
        NightEvent(
            id = "companion_confession",
            title = "A Quiet Confession",
            narrative = "A companion sits apart from the group, staring into the embers. Something weighs on them. They look like they want to talk.",
            choices = listOf(
                NightEventChoice("Sit beside them and listen", moraleDelta = 0.07f, outcome = "They share a memory from before the Hollow -- painful, but real. The bond between you deepens."),
                NightEventChoice("Give them space", moraleDelta = 0.01f, outcome = "They nod when you look over, grateful for the privacy. Some wounds heal alone."),
                NightEventChoice("Tell them to focus on the mission", moraleDelta = -0.04f, outcome = "They stiffen and turn away. The distance between you grows colder.")
            )
        ),
        NightEvent(
            id = "supplies_stolen",
            title = "The Silent Thief",
            narrative = "Dawn reveals that something raided the supplies overnight. Tracks lead toward the ruins -- small, clawed, deliberate.",
            choices = listOf(
                NightEventChoice("Track the thief", moraleDelta = 0.02f, outcome = "You follow the tracks to a nest of hollow-rats. You recover most of the supplies and find a curious artifact."),
                NightEventChoice("Set a trap for tonight", moraleDelta = 0.04f, outcome = "The trap works. Whatever it was won't be back. The group sleeps easier."),
                NightEventChoice("Accept the loss", moraleDelta = -0.05f, outcome = "Rations are shorter now. Nobody says anything, but the tension is palpable.")
            )
        ),
        NightEvent(
            id = "ancient_song",
            title = "Song of the Stones",
            narrative = "A melody drifts from the ruins -- hauntingly beautiful, wordless, ancient. It seems to come from the stones themselves.",
            choices = listOf(
                NightEventChoice("Follow the melody", moraleDelta = 0.03f, outcome = "The song leads to a carved alcove with faded murals of the Hollow King's court. You learn something about the past."),
                NightEventChoice("Hum along", moraleDelta = 0.06f, outcome = "The melody harmonizes with your voice. For a moment, the Hollow feels less hostile. Everyone sleeps peacefully."),
                NightEventChoice("Block it out", moraleDelta = -0.02f, outcome = "You stuff cloth in your ears. The song fades. But something feels lost.")
            )
        ),
        NightEvent(
            id = "watch_fire_dies",
            title = "The Fire Goes Out",
            narrative = "The watch-fire gutters and dies without warning. Not wind -- something else. The darkness presses close.",
            choices = listOf(
                NightEventChoice("Relight it immediately", moraleDelta = -0.01f, outcome = "The fire catches again, but the shadows seem thicker now. No one sleeps well."),
                NightEventChoice("Wait in the dark", moraleDelta = -0.04f, outcome = "Minutes stretch into hours. When the fire finally reignites on its own, everyone is shaken."),
                NightEventChoice("Gather the group close", moraleDelta = 0.03f, outcome = "You form a tight circle. Someone starts talking softly. By the time the fire returns, you've grown closer.")
            )
        ),
        NightEvent(
            id = "dream_vision",
            title = "The King's Dream",
            narrative = "You dream of a crowned figure on a throne of ash. He speaks without words, and you understand: 'Find what was taken. Return what was broken.'",
            choices = listOf(
                NightEventChoice("Try to speak back", moraleDelta = 0.02f, outcome = "The figure tilts its head. For a heartbeat, you see sorrow in hollow eyes. Then you wake."),
                NightEventChoice("Reach for the crown", moraleDelta = -0.03f, outcome = "The crown burns. You wake gasping, your hand tingling. Power has a price."),
                NightEventChoice("Turn away", moraleDelta = 0.01f, outcome = "The figure watches you go. You wake feeling strangely lighter, as if a burden was almost placed on you.")
            )
        )
    )

    private val tensionEvents = setOf("strange_noise", "companion_argument", "supplies_stolen", "watch_fire_dies")
    private val hopeEvents = setOf("clear_skies", "companion_confession", "ancient_song")

    fun getRandomEvent(morale: Float): NightEvent {
        return when {
            morale < 0.3f && Random.nextFloat() < 0.6f ->
                events.filter { it.id in tensionEvents }.random()
            morale > 0.7f && Random.nextFloat() < 0.4f ->
                events.filter { it.id in hopeEvents }.random()
            else -> events.random()
        }
    }
}
