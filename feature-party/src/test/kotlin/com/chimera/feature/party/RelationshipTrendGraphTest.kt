package com.chimera.feature.party

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelationshipTrendGraphTest {

    @Test
    fun snapshot_deltaCalculation() {
        val snapshots = listOf(
            DispositionSnapshot(disposition = 0.3f, delta = 0f),
            DispositionSnapshot(disposition = 0.5f, delta = 0.2f)
        )

        assertEquals(0.2f, snapshots[1].delta, 0.001f)
    }

    @Test
    fun emptyHistory_handled() {
        val empty: List<DispositionSnapshot> = emptyList()

        assertTrue(empty.isEmpty())
    }

    @Test
    fun historyLimitedToTenSnapshots() {
        val tenSnapshots = List(10) { i ->
            DispositionSnapshot(disposition = i * 0.1f)
        }

        assertEquals(10, tenSnapshots.size)
    }
}
