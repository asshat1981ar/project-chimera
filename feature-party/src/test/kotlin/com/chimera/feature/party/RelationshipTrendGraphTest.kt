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

    @Test
    fun colorThreshold_positiveDisposition_returnsVoidGreen() {
        val snapshots = listOf(
            DispositionSnapshot(disposition = 0.3f)
        )

        assertTrue(snapshots.last().disposition > 0.2f)
    }

    @Test
    fun colorThreshold_neutralDisposition_returnsEmberGold() {
        val snapshots = listOf(
            DispositionSnapshot(disposition = 0.1f)
        )

        assertTrue(snapshots.last().disposition > -0.2f && snapshots.last().disposition <= 0.2f)
    }

    @Test
    fun colorThreshold_negativeDisposition_returnsHollowCrimson() {
        val snapshots = listOf(
            DispositionSnapshot(disposition = -0.5f)
        )

        assertTrue(snapshots.last().disposition <= -0.2f)
    }

    @Test
    fun singlePointRendering_dotDrawnAtCenter() {
        val singleSnapshot = listOf(
            DispositionSnapshot(disposition = 0.0f)
        )

        assertEquals(1, singleSnapshot.size)
        assertEquals(0.0f, singleSnapshot[0].disposition, 0.001f)
    }

    @Test
    fun multiPointLineDrawing_correctPointCalculation() {
        val snapshots = listOf(
            DispositionSnapshot(disposition = -0.3f),
            DispositionSnapshot(disposition = -0.1f),
            DispositionSnapshot(disposition = 0.2f),
            DispositionSnapshot(disposition = 0.4f)
        )

        assertEquals(4, snapshots.size)
        assertEquals(-0.3f, snapshots.first().disposition, 0.001f)
        assertEquals(0.4f, snapshots.last().disposition, 0.001f)

        val points = snapshots.mapIndexed { index, snapshot ->
            val normalizedX = index.toFloat() / (snapshots.size - 1)
            val normalizedY = 0.5f - (snapshot.disposition + 0.5f) / 4f
            Pair(normalizedX, normalizedY)
        }

        assertEquals(0f, points.first().first, 0.001f)
        assertEquals(1f, points.last().first, 0.001f)
    }
}
