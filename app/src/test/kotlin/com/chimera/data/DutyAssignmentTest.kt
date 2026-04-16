package com.chimera.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DutyAssignmentTest {

    @Test
    fun `guard duty has negative morale effect`() {
        assertTrue(DutyType.GUARD.moraleEffect < 0f)
    }

    @Test
    fun `forage duty has positive morale effect`() {
        assertTrue(DutyType.FORAGE.moraleEffect > 0f)
    }

    @Test
    fun `rest duty has highest morale effect`() {
        assertTrue(DutyType.REST.moraleEffect > DutyType.FORAGE.moraleEffect)
    }

    @Test
    fun `all duty types have labels`() {
        DutyType.values().forEach { duty ->
            assertTrue(duty.label.isNotBlank())
            assertTrue(duty.description.isNotBlank())
        }
    }

    @Test
    fun `duty assignment defaults to no duty`() {
        val assignment = DutyAssignment("comp1", "Elena")
        assertEquals(null, assignment.duty)
    }

    @Test
    fun `duty assignment with duty set`() {
        val assignment = DutyAssignment("comp1", "Elena", DutyType.GUARD)
        assertEquals(DutyType.GUARD, assignment.duty)
        assertEquals("comp1", assignment.companionId)
    }
}
