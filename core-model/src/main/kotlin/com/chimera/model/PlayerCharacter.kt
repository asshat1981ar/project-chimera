package com.chimera.model

import kotlinx.serialization.Serializable

/**
 * Player character with full RPG attributes, progression, and state.
 */
@Serializable
data class PlayerCharacter(
    val id: String = "player",
    val name: String = "Wanderer",
    val level: Int = 1,
    val experience: Int = 0,
    val attributes: Attributes = Attributes(),
    val health: HealthState = HealthState(),
    val inventory: Inventory = Inventory(),
    val equipped: Equipment = Equipment(),
    val skills: SkillTree = SkillTree(),
    val reputation: Map<String, Float> = emptyMap(),
    val questLog: List<String> = emptyList(),
    val locationId: String = "start_village"
)

@Serializable
data class Attributes(
    val strength: Int = 10,
    val agility: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    val luck: Int = 5
) {
    fun total(): Int = strength + agility + constitution + intelligence + wisdom + charisma + luck

    fun modifier(attribute: Int): Int = (attribute - 10) / 2
    fun strMod(): Int = modifier(strength)
    fun dexMod(): Int = modifier(agility)
    fun conMod(): Int = modifier(constitution)
    fun intMod(): Int = modifier(intelligence)
    fun wisMod(): Int = modifier(wisdom)
    fun chaMod(): Int = modifier(charisma)
}

@Serializable
data class HealthState(
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val currentMp: Int = 50,
    val maxMp: Int = 50,
    val isAlive: Boolean = true
) {
    fun hpPercent(): Float = currentHp.toFloat() / maxHp.toFloat()
    fun mpPercent(): Float = currentMp.toFloat() / maxMp.toFloat()
    fun isCritical(): Boolean = hpPercent() < 0.25f
}

@Serializable
data class Inventory(
    val slots: List<ItemStack> = emptyList(),
    val capacity: Int = 20,
    val gold: Int = 0
) {
    fun isFull(): Boolean = slots.size >= capacity
    fun itemCount(): Int = slots.sumOf { it.quantity }
}

@Serializable
data class ItemStack(
    val itemId: String,
    val name: String,
    val description: String = "",
    val quantity: Int = 1,
    val category: ItemCategory = ItemCategory.MISC,
    val rarity: Rarity = Rarity.COMMON,
    val stats: Map<String, Int> = emptyMap(),
    val isUsable: Boolean = false,
    val isEquippable: Boolean = false
)

@Serializable
enum class ItemCategory {
    WEAPON, ARMOR, SHIELD, HELMET, ACCESSORY,
    CONSUMABLE, MATERIAL, KEY_ITEM, MISC
}

@Serializable
enum class Rarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
}

@Serializable
data class Equipment(
    val weapon: ItemStack? = null,
    val armor: ItemStack? = null,
    val shield: ItemStack? = null,
    val helmet: ItemStack? = null,
    val ring1: ItemStack? = null,
    val ring2: ItemStack? = null,
    val amulet: ItemStack? = null
) {
    fun all(): List<ItemStack?> = listOf(weapon, armor, shield, helmet, ring1, ring2, amulet)
    fun totalStats(): Map<String, Int> {
        return all().filterNotNull().flatMap { it.stats.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.sum() }
    }
}

@Serializable
data class SkillTree(
    val unlocked: List<String> = emptyList(),
    val availablePoints: Int = 0,
    val totalPoints: Int = 0
) {
    fun isUnlocked(skillId: String): Boolean = skillId in unlocked
    fun canUnlock(skillId: String, prerequisites: List<String>): Boolean {
        return availablePoints > 0 && prerequisites.all { it in unlocked }
    }
}

@Serializable
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val prerequisites: List<String> = emptyList(),
    val manaCost: Int = 0,
    val cooldown: Int = 0, // turns
    val damageType: DamageType? = null,
    val baseDamage: Int = 0,
    val scalingStat: String? = null,
    val effect: StatusEffect? = null
)

@Serializable
enum class DamageType {
    PHYSICAL, FIRE, ICE, LIGHTNING, POISON, VOID, RADIANT
}

@Serializable
data class StatusEffect(
    val id: String,
    val name: String,
    val duration: Int, // turns
    val modifier: StatModifier
)

@Serializable
data class StatModifier(
    val attribute: String, // e.g. "strength", "hp_regen"
    val value: Int,
    val isPercent: Boolean = false
)
