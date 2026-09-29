package com.chimera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Immutable envelope wrapping a [GameEvent] for the append-only event log (ADR-002).
 *
 * A record is **never mutated**. The log is a totally ordered sequence per save slot,
 * ordered by [sequence]. Two devices can therefore merge logs by union on
 * `(slotId, sequence)` with no conflict resolution.
 *
 * @property eventId      globally unique id (UUID string) - dedupe key for cloud merge
 * @property slotId       owning save slot
 * @property sequence     monotonic, gap-free, starts at 0, unique per slot
 * @property occurredAt   wall-clock epoch millis (informational; ordering uses [sequence])
 * @property schemaVersion payload schema version for forward-compatible upcasting
 * @property event        the payload
 */
@Serializable
data class GameEventRecord(
    @SerialName("event_id")       val eventId: String,
    @SerialName("slot_id")        val slotId: Long,
    @SerialName("sequence")       val sequence: Long,
    @SerialName("occurred_at")    val occurredAt: Long,
    @SerialName("schema_version") val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    @SerialName("event")          val event: GameEvent
) {
    companion object {
        /** Bump when the *meaning* of an existing payload field changes. */
        const val CURRENT_SCHEMA_VERSION: Int = 1
    }
}

/**
 * JSON codec for [GameEventRecord]. Centralised so the on-disk and on-the-wire formats
 * can never drift apart.
 *
 * `ignoreUnknownKeys` lets a newer app read an older log (and vice-versa for additive
 * fields); `encodeDefaults` keeps records byte-stable for hashing/dedupe.
 */
object GameEventCodec {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    fun encode(record: GameEventRecord): String = json.encodeToString(GameEventRecord.serializer(), record)

    fun decode(raw: String): GameEventRecord = json.decodeFromString(GameEventRecord.serializer(), raw)

    fun encodeEvent(event: GameEvent): String = json.encodeToString(GameEvent.serializer(), event)

    fun decodeEvent(raw: String): GameEvent = json.decodeFromString(GameEvent.serializer(), raw)
}
