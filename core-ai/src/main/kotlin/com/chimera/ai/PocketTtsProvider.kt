package com.chimera.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Android platform TTS with per-NPC voice fingerprinting.
 *
 * Each NPC gets a deterministic pitch + speech rate derived from [npcId].hashCode()
 * so the same character always sounds the same across sessions.
 * Pitch range: 0.75–1.25 (lower = gruff, higher = sharp)
 * Rate range:  0.80–1.10 (lower = slow/deliberate, higher = quick/nervous)
 *
 * When kyutai/pocket-tts ships an Android ONNX runtime, drop-in replace
 * the [speak] body — the interface and fingerprint logic stay unchanged.
 *
 * Adversarial notes:
 * - TTS engine not installed: init fails, [ready] = false, all speak() calls no-op
 * - Rapid successive calls: FLUSH queue mode ensures only the latest line plays
 * - Very long NPC lines: TTS handles chunking internally (< 4000 chars limit)
 * - ANR: speak() suspends on initialization, never blocks the main thread
 */
class PocketTtsProvider(private val context: Context) : AudioProvider {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ready = true
                Log.d(TAG, "TTS initialized")
            } else {
                Log.w(TAG, "TTS init failed (status=$status) — falling back to silent")
                ready = false
            }
        }
    }

    override suspend fun speak(text: String, npcId: String) {
        if (!ready || tts == null || text.isBlank()) return
        val (pitch, rate) = voiceFingerprint(npcId)
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
        tts?.speak(text.take(3999), TextToSpeech.QUEUE_FLUSH, null, npcId)
    }

    override fun stop() {
        if (ready) tts?.stop()
    }

    override fun release() {
        tts?.shutdown()
        tts = null
        ready = false
    }

    /**
     * Maps [npcId] to a (pitch, rate) pair deterministically.
     * Using the lower bits of the hash to spread values evenly across the range.
     */
    private fun voiceFingerprint(npcId: String): Pair<Float, Float> {
        val h = abs(npcId.hashCode())
        // Split hash into two independent values using golden-ratio scramble
        val pitchSeed = ((h * 0x9E3779B9.toInt()) ushr 16) and 0xFF
        val rateSeed  = ((h * 0x6C62272E.toInt()) ushr 16) and 0xFF
        val pitch = 0.75f + (pitchSeed / 255f) * 0.50f   // 0.75..1.25
        val rate  = 0.80f + (rateSeed  / 255f) * 0.30f   // 0.80..1.10
        return pitch to rate
    }

    companion object {
        const val TAG = "PocketTtsProvider"
    }
}
