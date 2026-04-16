package com.chimera.ai

/**
 * Abstraction over NPC voice synthesis.
 *
 * Implementations:
 *  - [PocketTtsProvider] — Android TextToSpeech with per-NPC voice tuning
 *  - [SilentAudioProvider] — no-op, used when voice is disabled or TTS init fails
 *
 * All methods are safe to call from any coroutine dispatcher.
 * Implementations must never throw — failures are silently swallowed.
 */
interface AudioProvider {

    /**
     * Speak [text] aloud, tuning the voice to [npcId] for a consistent
     * per-character voice signature. No-op if unavailable or voice disabled.
     */
    suspend fun speak(text: String, npcId: String)

    /** Stop the current utterance immediately. */
    fun stop()

    /** Release resources. Call when the owning scope is destroyed. */
    fun release()
}
