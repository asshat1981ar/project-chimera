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
