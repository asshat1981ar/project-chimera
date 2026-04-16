package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items WHERE save_slot_id = :slotId ORDER BY category, name")
    fun observeAll(slotId: Long): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE save_slot_id = :slotId AND category = :category ORDER BY name")
    fun observeByCategory(slotId: Long, category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE save_slot_id = :slotId AND item_id = :itemId")
    suspend fun getByItemId(slotId: Long, itemId: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantity = quantity + :amount WHERE save_slot_id = :slotId AND item_id = :itemId")
    suspend fun addQuantity(slotId: Long, itemId: String, amount: Int)

    @Query("UPDATE inventory_items SET quantity = quantity - :amount WHERE save_slot_id = :slotId AND item_id = :itemId AND quantity >= :amount")
    suspend fun removeQuantity(slotId: Long, itemId: String, amount: Int): Int

    @Query("DELETE FROM inventory_items WHERE save_slot_id = :slotId AND item_id = :itemId AND quantity <= 0")
    suspend fun cleanupEmpty(slotId: Long, itemId: String)

    @Query("SELECT COUNT(*) FROM inventory_items WHERE save_slot_id = :slotId")
    fun observeItemCount(slotId: Long): Flow<Int>
}
