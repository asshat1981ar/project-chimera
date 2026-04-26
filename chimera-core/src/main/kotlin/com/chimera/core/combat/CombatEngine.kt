package com.chimera.core.combat

import com.chimera.model.Attributes
import com.chimera.model.DamageType
import com.chimera.model.PlayerCharacter
import com.chimera.model.StatusEffect
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Deterministic combat engine -- pure Kotlin, zero Android dependencies.
 * Handles turn-based encounters between player and NPC enemies.
 */
class CombatEngine(private val random: Random = Random.Default) {

    /**
     * Execute one combat turn. Returns the updated combat state.
     */
    fun executeTurn(
        state: CombatState,
        action: CombatAction
    ): CombatResult {
        val (attacker, defender) = when (action.source) {
            CombatantType.PLAYER -> state.player to state.activeEnemy
            CombatantType.ENEMY -> state.activeEnemy to state.player
        }

        return when (action.type) {
            ActionType.ATTACK -> resolveAttack(state, attacker, defender, action)
            ActionType.SKILL -> resolveSkill(state, attacker, defender, action)
            ActionType.ITEM -> resolveItem(state, action)
            ActionType.DEFEND -> resolveDefend(state, action)
            ActionType.FLEE -> resolveFlee(state, action)
        }
    }

    private fun resolveAttack(
        state: CombatState,
        attacker: Combatant,
        defender: Combatant,
        action: CombatAction
    ): CombatResult {
        val hitRoll = random.nextInt(1, 21) // d20
        val hitChance = calculateHitChance(attacker, defender)
        val isHit = hitRoll <= hitChance

        if (!isHit) {
            return CombatResult(
                state = state.copy(log = state.log + CombatLogEntry("${attacker.name} attacks... but misses!", LogType.MISS)),
                isOver = false,
                winner = null,
                xpGained = 0
            )
        }

        val damage = calculatePhysicalDamage(attacker, defender)
        val newDefender = defender.copy(currentHp = max(0, defender.currentHp - damage))
        val isOver = newDefender.currentHp <= 0
        val isCritical = hitRoll == 20

        val logText = if (isCritical) {
            "CRITICAL HIT! ${attacker.name} strikes ${defender.name} for $damage damage!"
        } else {
            "${attacker.name} hits ${defender.name} for $damage damage."
        }

        val updatedState = when (action.source) {
            CombatantType.PLAYER -> state.copy(
                activeEnemy = newDefender,
                log = state.log + CombatLogEntry(logText, if (isCritical) LogType.CRITICAL else LogType.HIT)
            )
            CombatantType.ENEMY -> state.copy(
                player = newDefender,
                log = state.log + CombatLogEntry(logText, if (isCritical) LogType.CRITICAL else LogType.HIT)
            )
        }

        return CombatResult(
            state = updatedState,
            isOver = isOver,
            winner = if (isOver) action.source else null,
            xpGained = if (isOver && action.source == CombatantType.PLAYER) calculateXpReward(state.activeEnemy) else 0
        )
    }

    private fun resolveSkill(
        state: CombatState,
        attacker: Combatant,
        defender: Combatant,
        action: CombatAction
    ): CombatResult {
        // TODO: Implement skill resolution with mana costs and status effects
        return resolveAttack(state, attacker, defender, action.copy(type = ActionType.ATTACK))
    }

    private fun resolveItem(state: CombatState, action: CombatAction): CombatResult {
        return CombatResult(
            state = state.copy(log = state.log + CombatLogEntry("${action.source} uses an item.", LogType.INFO)),
            isOver = false,
            winner = null,
            xpGained = 0
        )
    }

    private fun resolveDefend(state: CombatState, action: CombatAction): CombatResult {
        val updatedState = state.copy(
            isDefending = true,
            log = state.log + CombatLogEntry("${action.source} takes a defensive stance.", LogType.INFO)
        )
        return CombatResult(state = updatedState, isOver = false, winner = null, xpGained = 0)
    }

    private fun resolveFlee(state: CombatState, action: CombatAction): CombatResult {
        val fleeChance = 0.3f + (state.player.attributes.dexMod() * 0.05f)
        val success = random.nextFloat() < fleeChance
        return CombatResult(
            state = state.copy(
                fled = success,
                log = state.log + CombatLogEntry(
                    if (success) "Escaped successfully!" else "Failed to flee!",
                    if (success) LogType.INFO else LogType.MISS
                )
            ),
            isOver = success,
            winner = null,
            xpGained = 0
        )
    }

    // ---- Calculation helpers ----

    fun calculateHitChance(attacker: Combatant, defender: Combatant): Int {
        val base = 10 + attacker.attributes.dexMod()
        val dodge = defender.attributes.dexMod()
        return min(95, max(5, base - dodge + 50)) // Simple D&D-style hit chance
    }

    fun calculatePhysicalDamage(attacker: Combatant, defender: Combatant): Int {
        val baseDamage = random.nextInt(2, 9) + attacker.attributes.strMod()
        val defense = defender.attributes.conMod() / 2
        val weaponBonus = attacker.equipmentStats["damage"] ?: 0
        return max(1, baseDamage + weaponBonus - defense)
    }

    fun calculateXpReward(enemy: Combatant): Int {
        val baseXp = 25
        val levelMultiplier = enemy.level.coerceAtLeast(1)
        return baseXp * levelMultiplier
    }

    fun generateEnemy(playerLevel: Int): Combatant {
        val level = max(1, playerLevel + random.nextInt(-1, 3))
        return Combatant(
            id = "enemy_${System.currentTimeMillis()}",
            name = ENEMY_NAMES.random(random),
            level = level,
            attributes = Attributes(
                strength = 8 + level + random.nextInt(-2, 3),
                agility = 8 + level + random.nextInt(-2, 3),
                constitution = 8 + level + random.nextInt(-2, 3),
                intelligence = random.nextInt(5, 12),
                wisdom = random.nextInt(5, 12),
                charisma = random.nextInt(3, 8),
                luck = random.nextInt(1, 6)
            ),
            currentHp = 40 + (level * 10),
            maxHp = 40 + (level * 10),
            statusEffects = emptyList()
        )
    }

    companion object {
        private val ENEMY_NAMES = listOf(
            "Hollow Thrall", "Drowned Wretch", "Ashen Hound", "Shade Walker",
            "Cursed Vagrant", "Rotting Mariner", "Mourning Widow", "Sootstain",
            "Blighted Child", "The Hollowed One"
        )
    }
}

// ---- Combat State Model ----

data class CombatState(
    val player: Combatant,
    val activeEnemy: Combatant,
    val enemiesDefeated: Int = 0,
    val turnCount: Int = 0,
    val isDefending: Boolean = false,
    val fled: Boolean = false,
    val log: List<CombatLogEntry> = emptyList()
)

data class Combatant(
    val id: String,
    val name: String,
    val level: Int = 1,
    val attributes: Attributes = Attributes(),
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val currentMp: Int = 50,
    val maxMp: Int = 50,
    val statusEffects: List<StatusEffect> = emptyList(),
    val equipmentStats: Map<String, Int> = emptyMap()
) {
    val hpPercent: Float get() = currentHp.toFloat() / maxHp.toFloat()
    fun isAlive(): Boolean = currentHp > 0
}

data class CombatAction(
    val source: CombatantType,
    val type: ActionType,
    val targetId: String? = null,
    val payload: String? = null // skillId or itemId
)

enum class CombatantType { PLAYER, ENEMY }
enum class ActionType { ATTACK, SKILL, ITEM, DEFEND, FLEE }

data class CombatResult(
    val state: CombatState,
    val isOver: Boolean,
    val winner: CombatantType?,
    val xpGained: Int
)

data class CombatLogEntry(
    val text: String,
    val type: LogType
)

enum class LogType { HIT, CRITICAL, MISS, HEAL, DAMAGE, INFO, DEATH }
