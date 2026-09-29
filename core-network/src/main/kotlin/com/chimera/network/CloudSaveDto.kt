package com.chimera.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sent to POST /save */
@Serializable
data class CloudSaveRequest(
    @SerialName("slot_id")         val slotId: Long,
    @SerialName("player_name")     val playerName: String,
    @SerialName("chapter_tag")     val chapterTag: String,
    @SerialName("playtime_seconds") val playtimeSeconds: Long,
    @SerialName("save_data_json")  val saveDataJson: String = "{}"
)

/** Returned by GET /save/:slotId */
@Serializable
data class CloudSaveResponse(
    @SerialName("slot_id")         val slotId: Long,
    @SerialName("player_name")     val playerName: String,
    @SerialName("chapter_tag")     val chapterTag: String,
    @SerialName("playtime_seconds") val playtimeSeconds: Long,
    @SerialName("save_data_json")  val saveDataJson: String,
    @SerialName("updated_at")      val updatedAt: Long
)

/** Returned by POST /save and DELETE /save/:slotId */
@Serializable
data class CloudSaveAck(
    @SerialName("ok")          val ok: Boolean,
    @SerialName("slot_id")     val slotId: Long? = null,
    @SerialName("deleted")     val deleted: Long? = null,
    @SerialName("updated_at")  val updatedAt: Long? = null
)

/**
 * Sent to POST /save/:slotId/events - an incremental batch of event-log records
 * (ADR-002). Records are immutable, so the server can treat this as an idempotent
 * upsert keyed on `(slot_id, sequence)`.
 */
@Serializable
data class EventLogPushRequest(
    @SerialName("slot_id")        val slotId: Long,
    @SerialName("from_sequence")  val fromSequence: Long,
    @SerialName("records")        val records: List<String>
)

/** Returned by GET /save/:slotId/events?from_sequence=N */
@Serializable
data class EventLogPullResponse(
    @SerialName("slot_id")       val slotId: Long,
    @SerialName("records")       val records: List<String> = emptyList(),
    @SerialName("max_sequence")  val maxSequence: Long = -1L
)

/** Returned by POST /save/:slotId/events */
@Serializable
data class EventLogPushAck(
    @SerialName("ok")            val ok: Boolean,
    @SerialName("slot_id")       val slotId: Long? = null,
    @SerialName("accepted")      val accepted: Int = 0,
    @SerialName("max_sequence")  val maxSequence: Long = -1L
)
