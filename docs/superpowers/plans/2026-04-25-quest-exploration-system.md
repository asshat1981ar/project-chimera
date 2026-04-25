# Quest Exploration System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a first-class quest and exploration system with persistent objectives, map quest markers, Journal quest history, and a compact story-context objective chip across key Android game screens.

**Architecture:** Add shared quest models in `core-model`, persist quest/objective state in Room, expose progression through `core-data` repository APIs and domain use cases, then integrate immutable UI models into Compose feature screens. Keep quest progression deterministic and local; AI providers must not own quest state.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, Navigation Compose, coroutines/Flow, JUnit, Mockito Kotlin, existing Gradle convention plugins.

---

## Scope Check

The approved spec spans schema, domain progression, shared UI, and several feature screens. This plan keeps it as one coherent feature because each task produces a working layer consumed by the next task. Implement in order and keep the project buildable after each task.

Do not implement unrelated combat, AI, web, deployment, or chatbot work while executing this plan.

## File Structure

- Create `core-model/src/main/kotlin/com/chimera/model/Quest.kt`
  - Shared quest enums and immutable UI/domain-facing models.
- Modify `core-database/src/main/kotlin/com/chimera/database/entity/QuestEntity.kt`
  - Add pinned/outcome/updated fields to existing quest rows.
- Create `core-database/src/main/kotlin/com/chimera/database/entity/QuestObjectiveEntity.kt`
  - Room entity for ordered objective steps.
- Modify `core-database/src/main/kotlin/com/chimera/database/dao/QuestDao.kt`
  - Add pinned, history, and active query operations.
- Create `core-database/src/main/kotlin/com/chimera/database/dao/QuestObjectiveDao.kt`
  - DAO for objective ordering, target lookup, status transitions, and idempotent completion.
- Modify `core-database/src/main/kotlin/com/chimera/database/ChimeraGameDatabase.kt`
  - Register objective entity/DAO and add migration `8 -> 9`.
- Modify `core-database/src/main/kotlin/com/chimera/database/di/DatabaseModule.kt`
  - Provide `QuestObjectiveDao`.
- Create `core-database/src/test/kotlin/com/chimera/database/dao/QuestObjectiveDaoTest.kt`
  - In-memory Room tests for objective ordering and status transitions.
- Create `core-data/src/main/kotlin/com/chimera/data/repository/QuestRepository.kt`
  - Repository API and mapping between entities and shared models.
- Create `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ObserveActiveObjectiveUseCase.kt`
- Create `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ObserveMapQuestMarkersUseCase.kt`
- Create `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCase.kt`
- Create `domain/src/main/kotlin/com/chimera/domain/usecase/quest/PinQuestUseCase.kt`
- Create `domain/src/test/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCaseTest.kt`
  - Deterministic progression tests.
- Create `core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveComponents.kt`
  - `ObjectiveChip`, `ObjectiveContextPanel`, and small badges/rows.
- Modify `feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt`
- Modify `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
  - Show objective chip near continue context.
- Modify `feature-map/src/main/kotlin/com/chimera/feature/map/MapViewModel.kt`
- Modify `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`
  - Show quest markers and objective-aware node sheets.
- Modify `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalViewModel.kt`
- Modify `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`
  - Add Quests tab and history/step display.
- Modify `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneViewModel.kt`
  - Trigger quest progression when scenes complete.
- Modify `feature-camp/src/main/kotlin/com/chimera/feature/camp/CraftingViewModel.kt`
  - Trigger recipe/item objective progress after crafting success.

## Task 1: Shared Quest Models

**Files:**
- Create: `core-model/src/main/kotlin/com/chimera/model/Quest.kt`
- Test: `core-model/src/test/kotlin/com/chimera/model/ModelValidationTest.kt`

- [ ] **Step 1: Add failing model validation tests**

Append these tests to `ModelValidationTest.kt`:

```kotlin
@Test
fun `QuestObjectiveStatus blocking statuses exclude completed optional`() {
    assertTrue(QuestObjectiveStatus.ACTIVE.blocksQuestCompletion)
    assertTrue(QuestObjectiveStatus.HIDDEN.blocksQuestCompletion)
    assertFalse(QuestObjectiveStatus.COMPLETED.blocksQuestCompletion)
    assertFalse(QuestObjectiveStatus.OPTIONAL_COMPLETED.blocksQuestCompletion)
}

@Test
fun `ActiveObjectiveSummary primary action defaults to none`() {
    val summary = ActiveObjectiveSummary(
        questId = 1L,
        objectiveId = 2L,
        title = "Reach the Processional",
        storyContext = "The Warden's warning points toward the throne road."
    )

    assertEquals(ObjectivePrimaryAction.NONE, summary.primaryAction)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :core-model:testDebugUnitTest --tests com.chimera.model.ModelValidationTest
```

Expected: compilation fails because `QuestObjectiveStatus`, `ActiveObjectiveSummary`, and `ObjectivePrimaryAction` do not exist.

- [ ] **Step 3: Create shared quest models**

Create `core-model/src/main/kotlin/com/chimera/model/Quest.kt`:

```kotlin
package com.chimera.model

enum class QuestStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    CHANGED
}

enum class QuestObjectiveType {
    VISIT_LOCATION,
    COMPLETE_SCENE,
    SPEAK_TO_NPC,
    VERIFY_RUMOR,
    CRAFT_ITEM,
    DISCOVER_RECIPE,
    SURVIVE_CAMP_CONSEQUENCE
}

enum class QuestObjectiveStatus(val blocksQuestCompletion: Boolean) {
    HIDDEN(true),
    ACTIVE(true),
    COMPLETED(false),
    FAILED(true),
    OPTIONAL_COMPLETED(false)
}

enum class ObjectivePrimaryAction {
    NONE,
    OPEN_MAP,
    VIEW_JOURNAL,
    CONTINUE_SCENE
}

data class Quest(
    val id: Long,
    val saveSlotId: Long,
    val title: String,
    val description: String,
    val status: QuestStatus,
    val sourceSceneId: String? = null,
    val sourceNpcId: String? = null,
    val pinnedOrder: Int? = null,
    val outcomeText: String? = null,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

data class QuestObjective(
    val id: Long,
    val questId: Long,
    val stepIndex: Int,
    val type: QuestObjectiveType,
    val status: QuestObjectiveStatus,
    val isRequired: Boolean,
    val targetSceneId: String? = null,
    val targetMapNodeId: String? = null,
    val targetNpcId: String? = null,
    val targetRumorId: Long? = null,
    val targetRecipeId: String? = null,
    val targetItemId: String? = null,
    val title: String,
    val storyContext: String,
    val recentConsequence: String? = null,
    val knownRequirement: String? = null,
    val rewardHint: String? = null,
    val riskHint: String? = null,
    val activatedAt: Long? = null,
    val completedAt: Long? = null
)

data class QuestWithObjectives(
    val quest: Quest,
    val objectives: List<QuestObjective>
)

data class ActiveObjectiveSummary(
    val questId: Long,
    val objectiveId: Long,
    val title: String,
    val storyContext: String,
    val relatedNpcId: String? = null,
    val relatedLocationId: String? = null,
    val recentConsequence: String? = null,
    val knownRequirement: String? = null,
    val primaryAction: ObjectivePrimaryAction = ObjectivePrimaryAction.NONE
)

data class MapQuestMarker(
    val mapNodeId: String,
    val questId: Long,
    val objectiveId: Long,
    val title: String,
    val isActiveTarget: Boolean,
    val isLockedTarget: Boolean,
    val status: QuestObjectiveStatus
)
```

- [ ] **Step 4: Run model tests**

Run:

```bash
./gradlew :core-model:testDebugUnitTest --tests com.chimera.model.ModelValidationTest
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add core-model/src/main/kotlin/com/chimera/model/Quest.kt core-model/src/test/kotlin/com/chimera/model/ModelValidationTest.kt
git commit -m "feat(core-model): add quest objective models"
```

## Task 2: Room Quest Objective Schema

**Files:**
- Modify: `core-database/src/main/kotlin/com/chimera/database/entity/QuestEntity.kt`
- Create: `core-database/src/main/kotlin/com/chimera/database/entity/QuestObjectiveEntity.kt`
- Modify: `core-database/src/main/kotlin/com/chimera/database/dao/QuestDao.kt`
- Create: `core-database/src/main/kotlin/com/chimera/database/dao/QuestObjectiveDao.kt`
- Modify: `core-database/src/main/kotlin/com/chimera/database/ChimeraGameDatabase.kt`
- Modify: `core-database/src/main/kotlin/com/chimera/database/di/DatabaseModule.kt`
- Test: `core-database/src/test/kotlin/com/chimera/database/dao/QuestObjectiveDaoTest.kt`

- [ ] **Step 1: Write failing DAO test**

Create `core-database/src/test/kotlin/com/chimera/database/dao/QuestObjectiveDaoTest.kt`:

```kotlin
package com.chimera.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chimera.database.ChimeraGameDatabase
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.QuestObjectiveEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class QuestObjectiveDaoTest {
    private lateinit var db: ChimeraGameDatabase
    private lateinit var questDao: QuestDao
    private lateinit var objectiveDao: QuestObjectiveDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChimeraGameDatabase::class.java
        ).allowMainThreadQueries().build()
        questDao = db.questDao()
        objectiveDao = db.questObjectiveDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeNextIncomplete_returnsLowestActiveRequiredObjective() = runTest {
        val questId = questDao.insert(
            QuestEntity(saveSlotId = 1L, title = "Road to the Throne", description = "Find the Processional")
        )
        objectiveDao.insertAll(
            listOf(
                objective(questId, 0, "COMPLETED", "Done"),
                objective(questId, 1, "ACTIVE", "Reach the Processional"),
                objective(questId, 2, "ACTIVE", "Face the Hollow King")
            )
        )

        val next = objectiveDao.observeNextIncomplete(questId).first()

        assertEquals("Reach the Processional", next?.title)
    }

    @Test
    fun completeBySceneId_isIdempotent() = runTest {
        val questId = questDao.insert(
            QuestEntity(saveSlotId = 1L, title = "Speak to Vessa", description = "Learn what the shrine knows")
        )
        val objectiveId = objectiveDao.insert(
            objective(questId, 0, "ACTIVE", "Speak to Vessa", targetSceneId = "vessa_shrine")
        )

        objectiveDao.completeMatchingScene(1L, "vessa_shrine")
        objectiveDao.completeMatchingScene(1L, "vessa_shrine")

        val updated = objectiveDao.getById(objectiveId)
        assertEquals("COMPLETED", updated?.status)
    }

    @Test
    fun observeNextIncomplete_returnsNullWhenRequiredObjectivesComplete() = runTest {
        val questId = questDao.insert(
            QuestEntity(saveSlotId = 1L, title = "A Finished Path", description = "All required work is done")
        )
        objectiveDao.insert(objective(questId, 0, "COMPLETED", "Completed step"))

        val next = objectiveDao.observeNextIncomplete(questId).first()

        assertNull(next)
    }

    private fun objective(
        questId: Long,
        stepIndex: Int,
        status: String,
        title: String,
        targetSceneId: String? = null
    ) = QuestObjectiveEntity(
        questId = questId,
        stepIndex = stepIndex,
        type = "COMPLETE_SCENE",
        status = status,
        isRequired = true,
        targetSceneId = targetSceneId,
        title = title,
        storyContext = "Story context"
    )
}
```

- [ ] **Step 2: Add test dependency**

Add this library alias to `gradle/libs.versions.toml` so local JVM Room tests can use `ApplicationProvider`:

```toml
androidx-test-core = "1.5.0"

androidx-test-core = { module = "androidx.test:core", version.ref = "androidx-test-core" }
```

Then add to `core-database/build.gradle.kts`:

```kotlin
testImplementation(libs.androidx.test.core)
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
./gradlew :core-database:testDebugUnitTest --tests com.chimera.database.dao.QuestObjectiveDaoTest
```

Expected: compilation fails because `QuestObjectiveEntity`, `QuestObjectiveDao`, and new DB accessors do not exist.

- [ ] **Step 4: Update `QuestEntity`**

Modify `core-database/src/main/kotlin/com/chimera/database/entity/QuestEntity.kt`:

```kotlin
@ColumnInfo(name = "pinned_order")
val pinnedOrder: Int? = null,

@ColumnInfo(name = "outcome_text")
val outcomeText: String? = null,

@ColumnInfo(name = "updated_at")
val updatedAt: Long = System.currentTimeMillis(),
```

Keep existing constructor fields and append these after `completedAt`.

- [ ] **Step 5: Create `QuestObjectiveEntity`**

Create `core-database/src/main/kotlin/com/chimera/database/entity/QuestObjectiveEntity.kt`:

```kotlin
package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_objectives",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("quest_id"),
        Index("target_scene_id"),
        Index("target_map_node_id"),
        Index("target_npc_id"),
        Index(value = ["quest_id", "step_index"], unique = true)
    ]
)
data class QuestObjectiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "quest_id")
    val questId: Long,

    @ColumnInfo(name = "step_index")
    val stepIndex: Int,

    val type: String,
    val status: String = "ACTIVE",

    @ColumnInfo(name = "is_required")
    val isRequired: Boolean = true,

    @ColumnInfo(name = "target_scene_id")
    val targetSceneId: String? = null,

    @ColumnInfo(name = "target_map_node_id")
    val targetMapNodeId: String? = null,

    @ColumnInfo(name = "target_npc_id")
    val targetNpcId: String? = null,

    @ColumnInfo(name = "target_rumor_id")
    val targetRumorId: Long? = null,

    @ColumnInfo(name = "target_recipe_id")
    val targetRecipeId: String? = null,

    @ColumnInfo(name = "target_item_id")
    val targetItemId: String? = null,

    val title: String,

    @ColumnInfo(name = "story_context")
    val storyContext: String,

    @ColumnInfo(name = "recent_consequence")
    val recentConsequence: String? = null,

    @ColumnInfo(name = "known_requirement")
    val knownRequirement: String? = null,

    @ColumnInfo(name = "reward_hint")
    val rewardHint: String? = null,

    @ColumnInfo(name = "risk_hint")
    val riskHint: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "activated_at")
    val activatedAt: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
```

- [ ] **Step 6: Create `QuestObjectiveDao`**

Create `core-database/src/main/kotlin/com/chimera/database/dao/QuestObjectiveDao.kt`:

```kotlin
package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.QuestObjectiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestObjectiveDao {
    @Query("SELECT * FROM quest_objectives WHERE id = :id")
    suspend fun getById(id: Long): QuestObjectiveEntity?

    @Query("SELECT * FROM quest_objectives WHERE quest_id = :questId ORDER BY step_index ASC")
    fun observeByQuest(questId: Long): Flow<List<QuestObjectiveEntity>>

    @Query(
        "SELECT * FROM quest_objectives " +
            "WHERE quest_id = :questId AND is_required = 1 AND status IN ('HIDDEN', 'ACTIVE', 'FAILED') " +
            "ORDER BY step_index ASC LIMIT 1"
    )
    fun observeNextIncomplete(questId: Long): Flow<QuestObjectiveEntity?>

    @Query(
        "SELECT qo.* FROM quest_objectives qo " +
            "INNER JOIN quests q ON q.id = qo.quest_id " +
            "WHERE q.save_slot_id = :slotId AND q.status = 'active' " +
            "AND qo.target_map_node_id = :mapNodeId " +
            "ORDER BY q.pinned_order IS NULL, q.pinned_order ASC, q.created_at ASC, qo.step_index ASC"
    )
    fun observeByMapNode(slotId: Long, mapNodeId: String): Flow<List<QuestObjectiveEntity>>

    @Query(
        "SELECT qo.* FROM quest_objectives qo " +
            "INNER JOIN quests q ON q.id = qo.quest_id " +
            "WHERE q.save_slot_id = :slotId AND q.status = 'active' " +
            "AND qo.target_map_node_id IS NOT NULL " +
            "ORDER BY q.pinned_order IS NULL, q.pinned_order ASC, q.created_at ASC, qo.step_index ASC"
    )
    fun observeMapObjectives(slotId: Long): Flow<List<QuestObjectiveEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(objective: QuestObjectiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(objectives: List<QuestObjectiveEntity>): List<Long>

    @Query(
        "UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :completedAt " +
            "WHERE id = :objectiveId AND status != 'COMPLETED'"
    )
    suspend fun complete(objectiveId: Long, completedAt: Long = System.currentTimeMillis())

    @Query(
        "UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :completedAt " +
            "WHERE id IN (" +
            "SELECT qo.id FROM quest_objectives qo INNER JOIN quests q ON q.id = qo.quest_id " +
            "WHERE q.save_slot_id = :slotId AND qo.target_scene_id = :sceneId " +
            "AND qo.status IN ('ACTIVE', 'HIDDEN')" +
            ")"
    )
    suspend fun completeMatchingScene(slotId: Long, sceneId: String, completedAt: Long = System.currentTimeMillis())
}
```

- [ ] **Step 7: Update `QuestDao`**

Add these methods to `QuestDao`:

```kotlin
@Query("SELECT * FROM quests WHERE id = :id")
suspend fun getById(id: Long): QuestEntity?

@Query("SELECT * FROM quests WHERE id = :id")
fun observeById(id: Long): Flow<QuestEntity?>

@Query(
    "SELECT * FROM quests WHERE save_slot_id = :slotId AND status = 'active' " +
        "ORDER BY pinned_order IS NULL, pinned_order ASC, created_at ASC"
)
fun observeActiveByPriority(slotId: Long): Flow<List<QuestEntity>>

@Query("UPDATE quests SET pinned_order = NULL WHERE save_slot_id = :slotId")
suspend fun clearPinned(slotId: Long)

@Query("UPDATE quests SET pinned_order = 0, updated_at = :updatedAt WHERE id = :questId")
suspend fun pin(questId: Long, updatedAt: Long = System.currentTimeMillis())

@Query("UPDATE quests SET status = 'completed', outcome_text = :outcomeText, completed_at = :completedAt, updated_at = :completedAt WHERE id = :id")
suspend fun complete(id: Long, outcomeText: String?, completedAt: Long = System.currentTimeMillis())
```

Replace the existing `complete(id, completedAt)` method with this new overload. Update existing callers to pass `outcomeText = null`.

- [ ] **Step 8: Register DB entity, DAO, and migration**

In `ChimeraGameDatabase.kt`:

```kotlin
import com.chimera.database.dao.QuestObjectiveDao
import com.chimera.database.entity.QuestObjectiveEntity
```

Add `QuestObjectiveEntity::class` to `entities`, change `version = 9`, add:

```kotlin
abstract fun questObjectiveDao(): QuestObjectiveDao
```

Add `.addMigrations(MIGRATION_7_8, MIGRATION_8_9)` and define:

```kotlin
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE quests ADD COLUMN pinned_order INTEGER")
        db.execSQL("ALTER TABLE quests ADD COLUMN outcome_text TEXT")
        db.execSQL("ALTER TABLE quests ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quest_objectives (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                quest_id INTEGER NOT NULL,
                step_index INTEGER NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                is_required INTEGER NOT NULL DEFAULT 1,
                target_scene_id TEXT,
                target_map_node_id TEXT,
                target_npc_id TEXT,
                target_rumor_id INTEGER,
                target_recipe_id TEXT,
                target_item_id TEXT,
                title TEXT NOT NULL,
                story_context TEXT NOT NULL,
                recent_consequence TEXT,
                known_requirement TEXT,
                reward_hint TEXT,
                risk_hint TEXT,
                created_at INTEGER NOT NULL,
                activated_at INTEGER,
                completed_at INTEGER,
                FOREIGN KEY(quest_id) REFERENCES quests(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quest_objectives_quest_id ON quest_objectives(quest_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quest_objectives_target_scene_id ON quest_objectives(target_scene_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quest_objectives_target_map_node_id ON quest_objectives(target_map_node_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quest_objectives_target_npc_id ON quest_objectives(target_npc_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_quest_objectives_quest_id_step_index ON quest_objectives(quest_id, step_index)")
    }
}
```

In `DatabaseModule.kt`, provide:

```kotlin
@Provides fun provideQuestObjectiveDao(db: ChimeraGameDatabase): QuestObjectiveDao = db.questObjectiveDao()
```

- [ ] **Step 9: Run database tests**

Run:

```bash
./gradlew :core-database:testDebugUnitTest --tests com.chimera.database.dao.QuestObjectiveDaoTest
```

Expected: pass.

- [ ] **Step 10: Generate schema**

Run:

```bash
./gradlew :core-database:kaptMockDebugKotlin
```

Expected: `core-database/schemas/com.chimera.database.ChimeraGameDatabase/9.json` is generated or updated.

- [ ] **Step 11: Commit**

```bash
git add gradle/libs.versions.toml core-database/build.gradle.kts core-database/src/main/kotlin/com/chimera/database core-database/src/test/kotlin/com/chimera/database/dao/QuestObjectiveDaoTest.kt core-database/schemas
git commit -m "feat(core-database): add quest objectives schema"
```

## Task 3: Quest Repository

**Files:**
- Create: `core-data/src/main/kotlin/com/chimera/data/repository/QuestRepository.kt`
- Test: `core-data/src/test/kotlin/com/chimera/data/repository/QuestRepositoryTest.kt`

- [ ] **Step 1: Write failing repository test**

Create `core-data/src/test/kotlin/com/chimera/data/repository/QuestRepositoryTest.kt`:

```kotlin
package com.chimera.data.repository

import com.chimera.database.dao.QuestDao
import com.chimera.database.dao.QuestObjectiveDao
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.QuestObjectiveEntity
import com.chimera.model.ObjectivePrimaryAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class QuestRepositoryTest {
    private val questDao: QuestDao = mock()
    private val objectiveDao: QuestObjectiveDao = mock()

    @Test
    fun mapActiveObjectiveSummary_prefersMapActionWhenLocationTargetExists() = runTest {
        val quest = QuestEntity(saveSlotId = 1L, title = "Road", description = "Find the road")
        val objective = QuestObjectiveEntity(
            id = 9L,
            questId = 7L,
            stepIndex = 0,
            type = "VISIT_LOCATION",
            status = "ACTIVE",
            isRequired = true,
            targetMapNodeId = "hollow_approach",
            title = "Reach the Processional",
            storyContext = "The Warden's warning points toward the throne road."
        )

        whenever(questDao.observeActiveByPriority(1L)).thenReturn(MutableStateFlow(listOf(quest.copy(id = 7L))))
        whenever(objectiveDao.observeNextIncomplete(7L)).thenReturn(MutableStateFlow(objective))

        val repository = QuestRepository(questDao, objectiveDao)
        val summary = repository.observeActiveObjective(1L).first()

        assertEquals(ObjectivePrimaryAction.OPEN_MAP, summary?.primaryAction)
        assertEquals("hollow_approach", summary?.relatedLocationId)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :core-data:testDebugUnitTest --tests com.chimera.data.repository.QuestRepositoryTest
```

Expected: compilation fails because `QuestRepository` does not exist.

- [ ] **Step 3: Implement repository**

Create `core-data/src/main/kotlin/com/chimera/data/repository/QuestRepository.kt`:

```kotlin
package com.chimera.data.repository

import com.chimera.database.dao.QuestDao
import com.chimera.database.dao.QuestObjectiveDao
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.QuestObjectiveEntity
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.MapQuestMarker
import com.chimera.model.ObjectivePrimaryAction
import com.chimera.model.Quest
import com.chimera.model.QuestObjective
import com.chimera.model.QuestObjectiveStatus
import com.chimera.model.QuestObjectiveType
import com.chimera.model.QuestStatus
import com.chimera.model.QuestWithObjectives
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestRepository @Inject constructor(
    private val questDao: QuestDao,
    private val objectiveDao: QuestObjectiveDao
) {
    fun observeActiveObjective(slotId: Long): Flow<ActiveObjectiveSummary?> =
        questDao.observeActiveByPriority(slotId).flatMapLatest { quests ->
            val quest = quests.firstOrNull() ?: return@flatMapLatest flowOf(null)
            objectiveDao.observeNextIncomplete(quest.id).map { objective ->
                objective?.toSummary(quest)
            }
        }

    fun observeActiveQuests(slotId: Long): Flow<List<Quest>> =
        questDao.observeActiveByPriority(slotId).map { quests -> quests.map { it.toModel() } }

    fun observeQuestHistory(slotId: Long): Flow<List<Quest>> =
        questDao.observeAll(slotId).map { quests -> quests.map { it.toModel() } }

    fun observeMapQuestMarkers(slotId: Long): Flow<List<MapQuestMarker>> =
        objectiveDao.observeMapObjectives(slotId).map { objectives ->
            objectives.mapNotNull { objective ->
                val nodeId = objective.targetMapNodeId ?: return@mapNotNull null
                MapQuestMarker(
                    mapNodeId = nodeId,
                    questId = objective.questId,
                    objectiveId = objective.id,
                    title = objective.title,
                    isActiveTarget = objective.status == "ACTIVE",
                    isLockedTarget = objective.status == "HIDDEN",
                    status = objective.status.toObjectiveStatus()
                )
            }
        }

    fun observeQuestWithObjectives(questId: Long): Flow<QuestWithObjectives?> =
        combine(
            questDao.observeById(questId),
            objectiveDao.observeByQuest(questId)
        ) { quest, objectives ->
            quest?.let { QuestWithObjectives(it.toModel(), objectives.map { objective -> objective.toModel() }) }
        }

    suspend fun pinQuest(slotId: Long, questId: Long) {
        questDao.clearPinned(slotId)
        questDao.pin(questId)
    }

    suspend fun completeSceneObjectives(slotId: Long, sceneId: String) {
        objectiveDao.completeMatchingScene(slotId, sceneId)
    }

    private fun QuestObjectiveEntity.toSummary(quest: QuestEntity): ActiveObjectiveSummary =
        ActiveObjectiveSummary(
            questId = quest.id,
            objectiveId = id,
            title = title,
            storyContext = storyContext,
            relatedNpcId = targetNpcId,
            relatedLocationId = targetMapNodeId,
            recentConsequence = recentConsequence,
            knownRequirement = knownRequirement,
            primaryAction = when {
                targetMapNodeId != null -> ObjectivePrimaryAction.OPEN_MAP
                targetSceneId != null -> ObjectivePrimaryAction.CONTINUE_SCENE
                else -> ObjectivePrimaryAction.VIEW_JOURNAL
            }
        )

    private fun QuestEntity.toModel(): Quest =
        Quest(
            id = id,
            saveSlotId = saveSlotId,
            title = title,
            description = description,
            status = status.toQuestStatus(),
            sourceSceneId = sourceSceneId,
            sourceNpcId = sourceNpcId,
            pinnedOrder = pinnedOrder,
            outcomeText = outcomeText,
            createdAt = createdAt,
            completedAt = completedAt
        )

    private fun QuestObjectiveEntity.toModel(): QuestObjective =
        QuestObjective(
            id = id,
            questId = questId,
            stepIndex = stepIndex,
            type = type.toObjectiveType(),
            status = status.toObjectiveStatus(),
            isRequired = isRequired,
            targetSceneId = targetSceneId,
            targetMapNodeId = targetMapNodeId,
            targetNpcId = targetNpcId,
            targetRumorId = targetRumorId,
            targetRecipeId = targetRecipeId,
            targetItemId = targetItemId,
            title = title,
            storyContext = storyContext,
            recentConsequence = recentConsequence,
            knownRequirement = knownRequirement,
            rewardHint = rewardHint,
            riskHint = riskHint,
            activatedAt = activatedAt,
            completedAt = completedAt
        )

    private fun String.toQuestStatus(): QuestStatus =
        runCatching { QuestStatus.valueOf(uppercase()) }.getOrDefault(QuestStatus.ACTIVE)

    private fun String.toObjectiveStatus(): QuestObjectiveStatus =
        runCatching { QuestObjectiveStatus.valueOf(uppercase()) }.getOrDefault(QuestObjectiveStatus.ACTIVE)

    private fun String.toObjectiveType(): QuestObjectiveType =
        runCatching { QuestObjectiveType.valueOf(uppercase()) }.getOrDefault(QuestObjectiveType.COMPLETE_SCENE)
}
```

- [ ] **Step 4: Run repository test**

Run:

```bash
./gradlew :core-data:testDebugUnitTest --tests com.chimera.data.repository.QuestRepositoryTest
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add core-data/src/main/kotlin/com/chimera/data/repository/QuestRepository.kt core-data/src/test/kotlin/com/chimera/data/repository/QuestRepositoryTest.kt core-database/src/main/kotlin/com/chimera/database/dao/QuestDao.kt
git commit -m "feat(core-data): add quest repository"
```

## Task 4: Domain Quest Use Cases

**Files:**
- Create: `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ObserveActiveObjectiveUseCase.kt`
- Create: `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ObserveMapQuestMarkersUseCase.kt`
- Create: `domain/src/main/kotlin/com/chimera/domain/usecase/quest/PinQuestUseCase.kt`
- Create: `domain/src/main/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCase.kt`
- Test: `domain/src/test/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCaseTest.kt`

- [ ] **Step 1: Write failing use-case test**

Create `domain/src/test/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCaseTest.kt`:

```kotlin
package com.chimera.domain.usecase.quest

import com.chimera.data.repository.QuestRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ResolveQuestProgressUseCaseTest {
    private val repository: QuestRepository = mock()

    @Test
    fun `scene completed event completes matching scene objectives`() = runTest {
        val useCase = ResolveQuestProgressUseCase(repository)

        useCase.onSceneCompleted(slotId = 1L, sceneId = "vessa_shrine")

        verify(repository).completeSceneObjectives(1L, "vessa_shrine")
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :domain:testDebugUnitTest --tests com.chimera.domain.usecase.quest.ResolveQuestProgressUseCaseTest
```

Expected: compilation fails because quest use cases do not exist.

- [ ] **Step 3: Add observe use cases**

Create `ObserveActiveObjectiveUseCase.kt`:

```kotlin
package com.chimera.domain.usecase.quest

import com.chimera.data.repository.QuestRepository
import com.chimera.model.ActiveObjectiveSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveObjectiveUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(slotId: Long): Flow<ActiveObjectiveSummary?> =
        questRepository.observeActiveObjective(slotId)
}
```

Create `ObserveMapQuestMarkersUseCase.kt`:

```kotlin
package com.chimera.domain.usecase.quest

import com.chimera.data.repository.QuestRepository
import com.chimera.model.MapQuestMarker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMapQuestMarkersUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(slotId: Long): Flow<List<MapQuestMarker>> =
        questRepository.observeMapQuestMarkers(slotId)
}
```

- [ ] **Step 4: Add pin and progress use cases**

Create `PinQuestUseCase.kt`:

```kotlin
package com.chimera.domain.usecase.quest

import com.chimera.data.repository.QuestRepository
import javax.inject.Inject

class PinQuestUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    suspend operator fun invoke(slotId: Long, questId: Long) {
        questRepository.pinQuest(slotId, questId)
    }
}
```

Create `ResolveQuestProgressUseCase.kt`:

```kotlin
package com.chimera.domain.usecase.quest

import com.chimera.data.repository.QuestRepository
import javax.inject.Inject

class ResolveQuestProgressUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    suspend fun onSceneCompleted(slotId: Long, sceneId: String) {
        questRepository.completeSceneObjectives(slotId, sceneId)
    }
}
```

- [ ] **Step 5: Run domain test**

Run:

```bash
./gradlew :domain:testDebugUnitTest --tests com.chimera.domain.usecase.quest.ResolveQuestProgressUseCaseTest
```

Expected: pass.

- [ ] **Step 6: Commit**

```bash
git add domain/src/main/kotlin/com/chimera/domain/usecase/quest domain/src/test/kotlin/com/chimera/domain/usecase/quest
git commit -m "feat(domain): add quest progression use cases"
```

## Task 5: Shared Objective Compose Components

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveComponents.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/components/ObjectiveComponentsTest.kt`

- [ ] **Step 1: Write model-level component test**

Create `core-ui/src/test/kotlin/com/chimera/ui/components/ObjectiveComponentsTest.kt`:

```kotlin
package com.chimera.ui.components

import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.ObjectivePrimaryAction
import org.junit.Assert.assertEquals
import org.junit.Test

class ObjectiveComponentsTest {
    @Test
    fun actionLabel_mapsOpenMap() {
        val objective = ActiveObjectiveSummary(
            questId = 1L,
            objectiveId = 2L,
            title = "Reach the Processional",
            storyContext = "The road matters.",
            primaryAction = ObjectivePrimaryAction.OPEN_MAP
        )

        assertEquals("Open Map", objective.primaryActionLabel())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :core-ui:testDebugUnitTest --tests com.chimera.ui.components.ObjectiveComponentsTest
```

Expected: compilation fails because `primaryActionLabel()` does not exist.

- [ ] **Step 3: Add components**

Create `core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveComponents.kt`:

```kotlin
package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.ObjectivePrimaryAction
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone

fun ActiveObjectiveSummary.primaryActionLabel(): String =
    when (primaryAction) {
        ObjectivePrimaryAction.NONE -> "View Objective"
        ObjectivePrimaryAction.OPEN_MAP -> "Open Map"
        ObjectivePrimaryAction.VIEW_JOURNAL -> "View Journal"
        ObjectivePrimaryAction.CONTINUE_SCENE -> "Continue Scene"
    }

@Composable
fun ObjectiveChip(
    objective: ActiveObjectiveSummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (objective == null) return
    AssistChip(
        modifier = modifier
            .testTag("chip_active_objective")
            .semantics {
                contentDescription = "Active objective: ${objective.title}"
            },
        onClick = onClick,
        leadingIcon = {
            Icon(Icons.Default.Flag, contentDescription = null, tint = EmberGold)
        },
        label = {
            Text(
                text = objective.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
fun ObjectiveContextPanel(
    objective: ActiveObjectiveSummary,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("panel_objective_context"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EmberGold.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(objective.title, style = MaterialTheme.typography.titleMedium, color = EmberGold)
            Text(objective.storyContext, style = MaterialTheme.typography.bodyMedium, color = FadedBone)
            objective.recentConsequence?.let {
                Text("Recent: $it", style = MaterialTheme.typography.bodySmall, color = FadedBone)
            }
            objective.knownRequirement?.let {
                Text("Requires: $it", style = MaterialTheme.typography.bodySmall, color = FadedBone)
            }
            Row(modifier = Modifier.padding(top = 12.dp)) {
                Spacer(Modifier.width(0.dp))
                Button(onClick = onPrimaryAction, modifier = Modifier.testTag("btn_objective_primary_action")) {
                    Text(objective.primaryActionLabel())
                }
            }
        }
    }
}
```

- [ ] **Step 4: Run component tests**

Run:

```bash
./gradlew :core-ui:testDebugUnitTest --tests com.chimera.ui.components.ObjectiveComponentsTest
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveComponents.kt core-ui/src/test/kotlin/com/chimera/ui/components/ObjectiveComponentsTest.kt
git commit -m "feat(core-ui): add objective chip components"
```

## Task 6: Home Objective Chip Integration

**Files:**
- Modify: `feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt`
- Modify: `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
- Test: `feature-home/src/test/kotlin/com/chimera/feature/home/HomeViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

Add a test that constructs `HomeViewModel` with a mocked `ObserveActiveObjectiveUseCase` returning a known objective. Assert `uiState.value.activeObjective?.title == "Reach the Processional"` after advancing the dispatcher.

Use this objective:

```kotlin
private val activeObjective = ActiveObjectiveSummary(
    questId = 1L,
    objectiveId = 2L,
    title = "Reach the Processional",
    storyContext = "The Warden's warning points toward the throne road.",
    relatedLocationId = "hollow_approach",
    primaryAction = ObjectivePrimaryAction.OPEN_MAP
)
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :feature-home:testDebugUnitTest --tests com.chimera.feature.home.HomeViewModelTest
```

Expected: compilation fails because `HomeUiState.activeObjective` and the new constructor dependency do not exist.

- [ ] **Step 3: Update `HomeViewModel`**

Add:

```kotlin
import com.chimera.domain.usecase.quest.ObserveActiveObjectiveUseCase
import com.chimera.model.ActiveObjectiveSummary
```

Add to `HomeUiState`:

```kotlin
val activeObjective: ActiveObjectiveSummary? = null,
```

Inject:

```kotlin
private val observeActiveObjectiveUseCase: ObserveActiveObjectiveUseCase,
```

In the `flatMapLatest` branch for a non-null slot, include:

```kotlin
observeActiveObjectiveUseCase(slotId)
```

in the `combine(...)` call and set `activeObjective = activeObjective` in `HomeUiState`.

- [ ] **Step 4: Update `HomeScreen`**

Import:

```kotlin
import com.chimera.ui.components.ObjectiveChip
```

After the greeting section and before the continue card, add:

```kotlin
item {
    ObjectiveChip(
        objective = uiState.activeObjective,
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    )
}
```

Use a no-op click only for this task. Navigation and panels are added later.

- [ ] **Step 5: Run feature-home tests**

Run:

```bash
./gradlew :feature-home:testDebugUnitTest
```

Expected: pass.

- [ ] **Step 6: Commit**

```bash
git add feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt feature-home/src/test/kotlin/com/chimera/feature/home/HomeViewModelTest.kt
git commit -m "feat(home): show active objective chip"
```

## Task 7: Map Quest Marker Integration

**Files:**
- Modify: `feature-map/src/main/kotlin/com/chimera/feature/map/MapViewModel.kt`
- Modify: `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`
- Test: `feature-map/src/test/kotlin/com/chimera/feature/map/MapViewModelTest.kt`

- [ ] **Step 1: Write failing map ViewModel test**

Add a test that mocks `ObserveMapQuestMarkersUseCase` with:

```kotlin
MapQuestMarker(
    mapNodeId = "hollow_approach",
    questId = 1L,
    objectiveId = 2L,
    title = "Reach the Processional",
    isActiveTarget = true,
    isLockedTarget = false,
    status = QuestObjectiveStatus.ACTIVE
)
```

Assert the emitted `MapUiState.questMarkers.size == 1`.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :feature-map:testDebugUnitTest --tests com.chimera.feature.map.MapViewModelTest
```

Expected: compilation fails because `questMarkers` and the use-case dependency do not exist.

- [ ] **Step 3: Update `MapUiState` and `MapViewModel`**

Add to `MapUiState`:

```kotlin
val questMarkers: List<MapQuestMarker> = emptyList(),
```

Inject:

```kotlin
private val observeMapQuestMarkersUseCase: ObserveMapQuestMarkersUseCase,
```

Include `observeMapQuestMarkersUseCase(slotId)` in the existing `combine(...)`, then set `questMarkers = questMarkers`.

- [ ] **Step 4: Update `MapScreen` marker display**

In the `uiState.nodes.forEach` call, compute:

```kotlin
val marker = uiState.questMarkers.firstOrNull { it.mapNodeId == node.id }
```

Pass `marker` into `MapNodeMarker`.

Inside `MapNodeMarker`, add a small quest indicator next to the existing rumor badge:

```kotlin
if (questMarker != null) {
    Badge(
        containerColor = if (questMarker.isActiveTarget) EmberGold else HollowCrimson,
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        Text("!")
    }
}
```

Update `NodeDetailSheet` to accept a `MapQuestMarker?` and show:

```kotlin
if (questMarker != null) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Objective: ${questMarker.title}",
        style = MaterialTheme.typography.bodySmall,
        color = EmberGold
    )
}
```

- [ ] **Step 5: Run map tests**

Run:

```bash
./gradlew :feature-map:testDebugUnitTest
```

Expected: pass.

- [ ] **Step 6: Commit**

```bash
git add feature-map/src/main/kotlin/com/chimera/feature/map/MapViewModel.kt feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt feature-map/src/test/kotlin/com/chimera/feature/map/MapViewModelTest.kt
git commit -m "feat(map): show quest objective markers"
```

## Task 8: Journal Quest History

**Files:**
- Modify: `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalViewModel.kt`
- Modify: `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`
- Test: `feature-journal/src/test/kotlin/com/chimera/feature/journal/JournalViewModelTest.kt`

- [ ] **Step 1: Write failing Journal test**

Add a mocked `QuestRepository` that emits one active quest. Assert `JournalUiState.quests.size == 1` and `JournalTab.QUESTS` exists.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :feature-journal:testDebugUnitTest --tests com.chimera.feature.journal.JournalViewModelTest
```

Expected: compilation fails because `QUESTS` tab and quest state do not exist.

- [ ] **Step 3: Add quest tab state**

Add to `JournalTab`:

```kotlin
QUESTS("Quests", null),
```

Add to `JournalUiState`:

```kotlin
val quests: List<Quest> = emptyList(),
```

Inject a quest history dependency:

```kotlin
private val questRepository: QuestRepository,
```

Combine `questRepository.observeQuestHistory(slotId)` into `uiState`.

- [ ] **Step 4: Add `QuestList` composable**

In `JournalScreen.kt`, branch:

```kotlin
when (uiState.selectedTab) {
    JournalTab.VOWS -> VowList(vows = uiState.vows)
    JournalTab.QUESTS -> QuestList(quests = uiState.quests)
    else -> EntryList(...)
}
```

Add:

```kotlin
@Composable
private fun QuestList(quests: List<Quest>) {
    if (quests.isEmpty()) {
        EmptyStateCard(
            title = "No quests yet.",
            message = "Exploration objectives will appear here.",
            modifier = Modifier.padding(16.dp)
        )
        return
    }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(quests, key = { it.id }) { quest ->
            Card(
                modifier = Modifier.testTag("card_quest_${quest.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, EmberGold.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(quest.title, style = MaterialTheme.typography.titleSmall, color = EmberGold)
                    Text(quest.description, style = MaterialTheme.typography.bodySmall, color = FadedBone)
                    Text(quest.status.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = DimAsh)
                }
            }
        }
    }
}
```

Import `EmptyStateCard` and `Quest`.

- [ ] **Step 5: Run Journal tests**

Run:

```bash
./gradlew :feature-journal:testDebugUnitTest
```

Expected: pass.

- [ ] **Step 6: Commit**

```bash
git add feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalViewModel.kt feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt feature-journal/src/test/kotlin/com/chimera/feature/journal/JournalViewModelTest.kt
git commit -m "feat(journal): add quest history tab"
```

## Task 9: Quest Progression Hooks

**Files:**
- Modify: `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneViewModel.kt`
- Modify: `feature-camp/src/main/kotlin/com/chimera/feature/camp/CraftingViewModel.kt`
- Test: existing ViewModel tests for dialogue and crafting.

- [ ] **Step 1: Add failing dialogue progression test**

In `DialogueSceneViewModelTest`, mock `ResolveQuestProgressUseCase`. Trigger the existing scene-complete path and verify:

```kotlin
verify(resolveQuestProgressUseCase).onSceneCompleted(slotId = 1L, sceneId = "watchtower_1")
```

- [ ] **Step 2: Update `DialogueSceneViewModel`**

Inject:

```kotlin
private val resolveQuestProgressUseCase: ResolveQuestProgressUseCase,
```

When the ViewModel marks a scene complete, call:

```kotlin
resolveQuestProgressUseCase.onSceneCompleted(slotId, sceneId)
```

Place the call after existing scene completion persistence so repeated retries remain idempotent.

- [ ] **Step 3: Add crafting progression after successful craft**

Extend `ResolveQuestProgressUseCase` with:

```kotlin
suspend fun onItemCrafted(slotId: Long, itemId: String, recipeId: String?) {
    questRepository.completeCraftingObjectives(slotId, itemId, recipeId)
}
```

Add matching repository/DAO methods following the `completeMatchingScene` pattern:

```sql
target_item_id = :itemId OR target_recipe_id = :recipeId
```

Call it from `CraftingViewModel` after a craft succeeds.

- [ ] **Step 4: Run targeted tests**

Run:

```bash
./gradlew :feature-dialogue:testDebugUnitTest :feature-camp:testDebugUnitTest :domain:testDebugUnitTest
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneViewModel.kt feature-dialogue/src/test/kotlin/com/chimera/feature/dialogue/DialogueSceneViewModelTest.kt feature-camp/src/main/kotlin/com/chimera/feature/camp/CraftingViewModel.kt feature-camp/src/test/kotlin/com/chimera/feature/camp/CraftingViewModelTest.kt domain/src/main/kotlin/com/chimera/domain/usecase/quest/ResolveQuestProgressUseCase.kt core-data/src/main/kotlin/com/chimera/data/repository/QuestRepository.kt core-database/src/main/kotlin/com/chimera/database/dao/QuestObjectiveDao.kt
git commit -m "feat(quest): resolve progress from gameplay events"
```

## Task 10: Verification And Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/sdlc/sprint-backlog.md`

- [ ] **Step 1: Run focused verification**

Run:

```bash
./gradlew :core-model:testDebugUnitTest :core-database:testDebugUnitTest :core-data:testDebugUnitTest :domain:testDebugUnitTest
```

Expected: all pass.

- [ ] **Step 2: Run feature verification**

Run:

```bash
./gradlew :feature-home:testDebugUnitTest :feature-map:testDebugUnitTest :feature-journal:testDebugUnitTest :feature-dialogue:testDebugUnitTest :feature-camp:testDebugUnitTest
```

Expected: all pass.

- [ ] **Step 3: Run Android build verification**

Run:

```bash
./gradlew testMockDebugUnitTest assembleMockDebug
```

Expected: build successful.

- [ ] **Step 4: Update README**

In `README.md`, add a short section under Architecture or Content:

```markdown
## Quest And Exploration System

Quests and objectives are deterministic local game state. The map shows quest-relevant nodes and the Journal preserves active/completed quest history. A compact objective chip on key screens gives current story context without depending on cloud AI.
```

- [ ] **Step 5: Update sprint backlog**

Add a new sprint entry to `docs/sdlc/sprint-backlog.md`:

```markdown
## Sprint 13 — Quest And Exploration System

**Goal:** Make objectives first-class gameplay through persistent quest steps, map markers, Journal history, and compact story-context UI.

**Scope:**
- Quest objective Room schema and migration.
- QuestRepository and domain progression use cases.
- ObjectiveChip and ObjectiveContextPanel in core-ui.
- Home, Map, Journal, Dialogue, and Crafting integration.

**Exit Criteria:**
- Quest/objective tests pass.
- `./gradlew testMockDebugUnitTest assembleMockDebug` passes.
- README documents deterministic quest state.
```

- [ ] **Step 6: Commit**

```bash
git add README.md docs/sdlc/sprint-backlog.md
git commit -m "docs: document quest exploration system"
```

## Final Acceptance

The feature is complete when:

- Room schema is version 9 with `quest_objectives`.
- Quest progression is deterministic and idempotent for scene and crafting events.
- Home shows the compact objective chip when an objective exists.
- Map shows quest markers and node-sheet objective context.
- Journal has a Quests tab with active/completed/failed quest history.
- Objective UI has accessibility labels/test tags.
- All verification commands in Task 10 pass from repository root.
