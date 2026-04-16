package com.chimera.data

enum class DutyType(val label: String, val description: String, val moraleEffect: Float) {
    GUARD("Guard", "Watch the perimeter through the night", -0.02f),
    FORAGE("Forage", "Search nearby for useful supplies", 0.03f),
    REST("Rest", "Sleep deeply and recover strength", 0.05f)
}

data class DutyAssignment(
    val companionId: String,
    val companionName: String,
    val duty: DutyType? = null
)
