package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only event log row (ADR-002).
 *
 * Rows are **never updated or deleted** (except by explicit log compaction, a follow-up
 * task). The `(slot_id, sequence)` pair is unique, which is what makes cloud merge a
 * conflict-free union.
 *
 * [payloadJson] holds the serialised [com.chimera.model.GameEventRecord]; it is stored as
 * text so the log survives schema evolution via `schema_version` upcasting.
 */
@Entity(
    tableName = "game_events",
    indices = [
        Index(value = ["slot_id", "sequence"], unique = true),
        Index(value = ["event_id"], unique = true),
        Index(value = ["slot_id"])
    ]
)
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "slot_id")
    val slotId: Long,

    @ColumnInfo(name = "sequence")
    val sequence: Long,

    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "payload_json")
    val payloadJson: String
)
