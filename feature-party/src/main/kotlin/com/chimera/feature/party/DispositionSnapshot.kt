package com.chimera.feature.party

data class DispositionSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val disposition: Float,
    val delta: Float = 0f
)
