package com.chimera.ai

/** No-op AudioProvider. Used when voice is disabled in settings or TTS unavailable. */
object SilentAudioProvider : AudioProvider {
    override suspend fun speak(text: String, npcId: String) = Unit
    override fun stop() = Unit
    override fun release() = Unit
}
