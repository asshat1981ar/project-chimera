package com.chimera.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chimera.ai.PortraitGenerationService
import com.chimera.database.dao.CharacterDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

/**
 * WorkManager worker that generates NPC portrait images via HuggingFace and
 * caches them to internal storage.
 *
 * Designed for fire-and-forget scheduling — the game plays fine without portraits,
 * so failures are logged and swallowed. WorkManager retries handle transient API errors.
 *
 * Input data:
 *   [KEY_SLOT_ID]: Long — the save slot to generate portraits for
 *
 * Each portrait is saved to: filesDir/portraits/npc_{characterId}.jpg
 * The file:// absolute path is stored in CharacterEntity.portraitResName.
 * Coil's AsyncImage loads file:// URIs natively.
 *
 * Adversarial notes:
 * - PortraitGenerationService returns null on any failure → skip that NPC, others continue
 * - File write failure (IOException) → caught per-NPC, others continue
 * - Worker killed mid-batch → each NPC is independently committed, no lost progress
 * - Character deleted between fetch and update → updatePortraitResName is a no-op on missing ID
 */
@HiltWorker
class NpcPortraitSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val characterDao: CharacterDao,
    private val portraitService: PortraitGenerationService
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val slotId = inputData.getLong(KEY_SLOT_ID, -1L)
        if (slotId < 0) {
            Log.w(TAG, "No valid slotId in input data; aborting")
            return Result.failure()
        }

        val npcs = characterDao.getBySlotWithoutPortrait(slotId)
        if (npcs.isEmpty()) {
            Log.d(TAG, "All NPCs for slot $slotId already have portraits")
            return Result.success()
        }

        Log.d(TAG, "Generating portraits for ${npcs.size} NPCs in slot $slotId")

        val portraitDir = File(appContext.filesDir, "portraits").apply { mkdirs() }
        var anyGenerated = false

        for (npc in npcs) {
            try {
                val bytes = portraitService.generatePortrait(
                    npcName  = npc.name,
                    npcRole  = npc.role,
                    npcTitle = npc.title
                ) ?: continue   // null = service failure; skip this NPC, try next

                val file = File(portraitDir, "npc_${npc.id}.jpg")
                file.writeBytes(bytes)

                characterDao.updatePortraitResName(npc.id, file.absolutePath)
                anyGenerated = true
                Log.d(TAG, "Portrait saved for ${npc.name} → ${file.name}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate portrait for ${npc.name}: ${e.message}")
                // Continue to next NPC — one failure shouldn't abort the batch
            }
        }

        return if (anyGenerated) Result.success() else Result.retry()
    }

    companion object {
        const val TAG           = "NpcPortraitSyncWorker"
        const val KEY_SLOT_ID   = "slot_id"
        const val WORK_NAME     = "npc_portrait_sync"
    }
}
