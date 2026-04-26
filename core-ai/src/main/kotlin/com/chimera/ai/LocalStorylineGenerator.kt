package com.chimera.ai

import com.chimera.model.Quest
import com.chimera.model.QuestStatus
import com.chimera.model.StoryArc
import com.chimera.model.StoryBeat

/**
 * Rule-based storyline generator -- deterministic, works offline.
 * Serves as the fallback when no AI provider is available.
 */
class LocalStorylineGenerator : StorylineGenerator {

    override suspend fun generateQuestChain(context: QuestGenerationContext): List<Quest> {
        val templates = QUEST_TEMPLATES.filter { template ->
            context.regionTags.any { template.tags.contains(it) }
        }.ifEmpty { QUEST_TEMPLATES }

        val count = (1..2).random()
        return templates.shuffled().take(count).mapIndexed { index, template ->
            template.toQuest(
                id = "${context.regionTags.firstOrNull() ?: "wild"}_${index}_${System.currentTimeMillis()}",
                level = context.playerLevel
            )
        }
    }

    override suspend fun generateStoryBeat(context: StoryBeatContext): StoryBeat {
        val templates = BEAT_TEMPLATES.filter { it.arcTag == context.currentArc.tag }
            .ifEmpty { BEAT_TEMPLATES }

        val template = templates.random()
        return StoryBeat(
            id = "beat_${System.currentTimeMillis()}",
            description = template.description,
            type = template.type,
            consequence = template.consequence,
            choices = template.choices
        )
    }

    override suspend fun isAvailable(): Boolean = true

    // ----- Templates -----

    private data class QuestTemplate(
        val name: String,
        val description: String,
        val tags: List<String>,
        val objectives: List<String>,
        val rewardType: String,
        val rewardAmount: Int
    )

    private fun QuestTemplate.toQuest(id: String, level: Int): Quest {
        return Quest(
            id = 0L, // Will be assigned by DAO
            saveSlotId = 0L,
            title = "$name (Lv.${level + 1})",
            description = this.description,
            status = QuestStatus.ACTIVE,
            sourceSceneId = null,
            sourceNpcId = null,
            pinnedOrder = null,
            outcomeText = null,
            createdAt = System.currentTimeMillis(),
            completedAt = null,
            totalSteps = this.objectives.size,
            currentStep = 0
        )
    }

    private data class BeatTemplate(
        val arcTag: String,
        val description: String,
        val type: String,
        val consequence: String,
        val choices: List<String>
    )

    companion object {
        private val QUEST_TEMPLATES = listOf(
            QuestTemplate(
                name = "Whispers in the Hollow",
                description = "Strange echoes emanate from the cursed caverns beneath the village.",
                tags = listOf("wilderness", "mystery", "hollow"),
                objectives = listOf("Investigate the cavern entrance", "Locate the source of whispers", "Decide the fate of the trapped spirit"),
                rewardType = "xp",
                rewardAmount = 50
            ),
            QuestTemplate(
                name = "The Sunken Reliquary",
                description = "A flooded temple holds the key to an ancient seal.",
                tags = listOf("ruins", "water", "artifact"),
                objectives = listOf("Drain the lower chamber", "Recover the reliquary shard", "Escape the drowned guardian"),
                rewardType = "artifact",
                rewardAmount = 1
            ),
            QuestTemplate(
                name = "Ashes of the Watch",
                description = "A fallen order's last keeper seeks a successor.",
                tags = listOf("civilized", "lore", "hollow"),
                objectives = listOf("Find the keeper's hidden sanctum", "Pass the trial of ash", "Swear or refuse the oath"),
                rewardType = "ability",
                rewardAmount = 1
            ),
            QuestTemplate(
                name = "The Last Ember",
                description = "A dying phoenix has chosen a mortal to carry its flame.",
                tags = listOf("wilderness", "legendary"),
                objectives = listOf("Rendezvous at the Ashfall Peak", "Protect the ember from hollow-beasts", "Deliver it to the forge-sage"),
                rewardType = "currency",
                rewardAmount = 200
            )
        )

        private val BEAT_TEMPLATES = listOf(
            BeatTemplate(
                arcTag = "hollow_awakening",
                description = "The ground trembles. Somewhere below, something vast shifts in its sleep.",
                type = "world_event",
                consequence = "world_hollow_level_increase",
                choices = listOf("Investigate the depths", "Warn nearby settlements", "Prepare defenses")
            ),
            BeatTemplate(
                arcTag = "hollow_awakening",
                description = "A survivor stumbles into camp, whispering of a city that should not exist.",
                type = "plot_hook",
                consequence = "unlock_quest_hidden_city",
                choices = listOf("Believe and follow", "Dismiss as ravings", "Interrogate carefully")
            ),
            BeatTemplate(
                arcTag = "personal_redemption",
                description = "The NPC you wronged stands in your path. They are not armed, but their stare cuts deeper than any blade.",
                type = "character_moment",
                consequence = "relationship_resolve_or_break",
                choices = listOf("Apologize sincerely", "Offer recompense", "Walk past in silence")
            )
        )
    }
}
