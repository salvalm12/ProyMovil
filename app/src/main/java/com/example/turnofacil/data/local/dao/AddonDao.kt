package com.example.turnofacil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.turnofacil.data.local.entity.AddonEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a adicionales.
 *
 * Igual que los servicios, la baja es lógica para no romper citas históricas.
 */
@Dao
interface AddonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(addon: AddonEntity): Long

    @Update
    suspend fun update(addon: AddonEntity): Int

    @Query("SELECT * FROM addon WHERE id = :id")
    suspend fun getById(id: Long): AddonEntity?

    @Query("SELECT * FROM addon WHERE name = :name AND active = 1 LIMIT 1")
    suspend fun getByName(name: String): AddonEntity?

    /** Adicionales disponibles para elegir en una cita. */
    @Query("SELECT * FROM addon WHERE active = 1 ORDER BY name")
    fun observeActive(): Flow<List<AddonEntity>>

    /** Todos los adicionales, incluidos los inactivos (administración). */
    @Query("SELECT * FROM addon ORDER BY name")
    fun observeAll(): Flow<List<AddonEntity>>

    /** Baja lógica: conserva la fila y solo apaga `active`. */
    @Query("UPDATE addon SET active = 0 WHERE id = :id")
    suspend fun softDelete(id: Long): Int
}
