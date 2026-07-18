package com.chimera.core.model.sprites

/**
 * Pure-Kotlin resolution contract for sprite assets.
 *
 * Extracted from SpriteManifest so that:
 * 1. Compose components depend on an interface, not the @Singleton Android-backed
 *    manifest (fixes SpriteTestFixtures illegally subclassing a final class).
 * 2. Tests and previews can supply in-memory resolvers with zero Android deps.
 *
 * All ID construction is centralized in [SpriteIds] so the manifest, resolvers,
 * fixtures, and the generation pipeline can never drift on naming conventions.
 */
interface SpriteResolver {
    /** Whether the backing store has been initialized. */
    val isLoaded: Boolean

    /** Total number of resolvable sprite entries. */
    val totalSprites: Int

    /** Exact lookup. Returns null when the asset is unknown (callers must fall back gracefully). */
    fun resolve(id: SpriteId): SpriteRef?

    /** Existence check without any loading side effects. */
    fun exists(id: SpriteId): Boolean = resolve(id) != null

    fun resolveNpcPortrait(npcId: String, expression: PortraitExpression): SpriteRef? =
        resolve(SpriteIds.npcPortrait(npcId, expression))

    fun resolveAllNpcPortraits(npcId: String): Map<PortraitExpression, SpriteRef> =
        PortraitExpression.entries.mapNotNull { expr ->
            resolveNpcPortrait(npcId, expr)?.let { expr to it }
        }.toMap()

    fun resolveNpcToken(npcId: String): SpriteRef? =
        resolve(SpriteIds.npcToken(npcId))

    fun resolveMapNode(nodeType: String, state: MapNodeState): SpriteRef? =
        resolve(SpriteIds.mapNode(nodeType, state))

    fun resolveCombatStance(isPlayer: Boolean, stanceName: String, wounded: Boolean = false): SpriteRef? =
        resolve(SpriteIds.combatStance(isPlayer, stanceName, wounded))

    fun resolveCampItem(itemId: String): SpriteRef? =
        resolve(SpriteIds.campItem(itemId))

    fun resolveUiElement(elementId: String): SpriteRef? =
        resolve(SpriteIds.uiElement(elementId))
}

/**
 * Canonical sprite ID construction. Single source of truth for the naming
 * conventions documented in SPRITE-DEVELOPMENT-PLAN.md §2/§5:
 *
 *   npc_{npcId}_{expression} · npc_{npcId}_token · map_{nodeType}_{state}
 *   combat_{player|opponent}_{stance}[_wounded] · camp_item_{itemId} · ui_{elementId}
 */
object SpriteIds {

    fun npcPortrait(npcId: String, expression: PortraitExpression): SpriteId =
        SpriteId("npc_${sanitize(npcId)}_${expression.expressionName}")

    fun npcToken(npcId: String): SpriteId =
        SpriteId("npc_${sanitize(npcId)}_token")

    fun mapNode(nodeType: String, state: MapNodeState): SpriteId =
        SpriteId("map_${sanitize(nodeType)}_${state.stateName}")

    fun combatStance(isPlayer: Boolean, stanceName: String, wounded: Boolean): SpriteId {
        val prefix = if (isPlayer) "combat_player" else "combat_opponent"
        val suffix = if (wounded) "_wounded" else ""
        return SpriteId("${prefix}_${sanitize(stanceName)}$suffix")
    }

    fun campItem(itemId: String): SpriteId =
        SpriteId("camp_item_${sanitize(itemId)}")

    fun uiElement(elementId: String): SpriteId =
        SpriteId("ui_${sanitize(elementId)}")

    /**
     * Normalizes free-form game identifiers into drawable-safe snake_case:
     * lowercase, spaces/hyphens to underscores, strip anything non [a-z0-9_].
     */
    fun sanitize(raw: String): String =
        raw.trim()
            .lowercase()
            .replace(Regex("[\\s-]+"), "_")
            .replace(Regex("[^a-z0-9_]"), "")
}

/** Resolver that knows no sprites. Useful for previews and fallback-path tests. */
object EmptySpriteResolver : SpriteResolver {
    override val isLoaded: Boolean = true
    override val totalSprites: Int = 0
    override fun resolve(id: SpriteId): SpriteRef? = null
}

/** Simple in-memory resolver over a fixed sprite map. */
class MapBackedSpriteResolver(
    private val sprites: Map<SpriteId, SpriteRef>
) : SpriteResolver {
    constructor(vararg refs: SpriteRef) : this(refs.associateBy { it.id })

    override val isLoaded: Boolean = true
    override val totalSprites: Int = sprites.size
    override fun resolve(id: SpriteId): SpriteRef? = sprites[id]
}
