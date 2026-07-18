package com.chimera.ui.components

import com.chimera.core.model.sprites.PortraitExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialogueToneRingTest {

    @Test fun `every expression has a tone label`() {
        PortraitExpression.entries.forEach { expression ->
            assertTrue(toneRingLabel(expression).isNotBlank())
        }
    }

    @Test fun `only hostile expressions pulse the ring`() {
        assertTrue(toneRingPulses(PortraitExpression.HOSTILE))
        PortraitExpression.entries.filter { it != PortraitExpression.HOSTILE }.forEach {
            assertFalse(toneRingPulses(it))
        }
    }

    @Test fun `ring colors differ between expressions so tone is not color-ambiguous at a glance`() {
        val colors = PortraitExpression.entries.map { toneRingColor(it) }.toSet()
        assertEquals(PortraitExpression.entries.size, colors.size)
    }
}

class MemoryRuneChipTest {

    @Test fun `stranger disposition shows no runes`() {
        assertTrue(memoryRunesForDisposition(0f).isEmpty())
        assertTrue(memoryRunesForDisposition(0.1f).isEmpty())
        assertTrue(memoryRunesForDisposition(-0.1f).isEmpty())
    }

    @Test fun `grateful threshold aligns with PortraitExpression`() {
        assertEquals(listOf(MemoryRune.GRATEFUL, MemoryRune.REMEMBERED),
            memoryRunesForDisposition(0.5f))
    }

    @Test fun `oath-bound outranks grateful above vow threshold`() {
        assertEquals(listOf(MemoryRune.OATH_BOUND, MemoryRune.REMEMBERED),
            memoryRunesForDisposition(0.8f))
    }

    @Test fun `suspicious band sits between neutral and wounded`() {
        assertEquals(listOf(MemoryRune.SUSPICIOUS, MemoryRune.REMEMBERED),
            memoryRunesForDisposition(-0.3f))
    }

    @Test fun `wounded shows for deeply negative disposition`() {
        assertEquals(listOf(MemoryRune.WOUNDED, MemoryRune.REMEMBERED),
            memoryRunesForDisposition(-0.6f))
        assertEquals(listOf(MemoryRune.WOUNDED, MemoryRune.REMEMBERED),
            memoryRunesForDisposition(-0.95f))
    }

    @Test fun `never more than two chips and no raw score exposure`() {
        (-100..100).forEach { i ->
            val runes = memoryRunesForDisposition(i / 100f)
            assertTrue(runes.size <= 2)
        }
    }

    @Test fun `runes never contradict PortraitExpression tone bands`() {
        (-100..100).forEach { i ->
            val d = i / 100f
            val runes = memoryRunesForDisposition(d)
            when (PortraitExpression.fromDisposition(d)) {
                PortraitExpression.OATHBOUND ->
                    assertTrue(MemoryRune.OATH_BOUND in runes)
                PortraitExpression.GRATEFUL ->
                    assertTrue(MemoryRune.GRATEFUL in runes)
                PortraitExpression.NEUTRAL ->
                    assertTrue(runes.none { it == MemoryRune.WOUNDED || it == MemoryRune.SUSPICIOUS })
                PortraitExpression.TENSE ->
                    assertTrue(MemoryRune.SUSPICIOUS in runes)
                PortraitExpression.WOUNDED, PortraitExpression.HOSTILE ->
                    assertTrue(MemoryRune.WOUNDED in runes)
            }
        }
    }
}
