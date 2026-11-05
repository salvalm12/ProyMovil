package com.example.turnofacil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.turnofacil.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a servicios.
 *
 * No existe `DELETE`: la baja es lógica para conservar el historial de citas
 * (regla de negocio 6).
 */
@Dao
interface ServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: ServiceEntity): Long

    @Update
    suspend fun update(service: ServiceEntity): Int

    @Query("SELECT * FROM service WHERE id = :id")
    suspend fun getById(id: Long): ServiceEntity?

    @Query("SELECT * FROM service WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): ServiceEntity?

    @Query("SELECT COUNT(*) FROM service")
    suspend fun count(): Int

    /** Catálogo visible para agendar: solo servicios activos. */
    @Query("SELECT * FROM service WHERE active = 1 ORDER BY name")
    fun observeActive(): Flow<List<ServiceEntity>>

    /** Todos los servicios, incluidos los inactivos (administración). */
    @Query("SELECT * FROM service ORDER BY name")
    fun observeAll(): Flow<List<ServiceEntity>>

    /**
     * Baja lógica: el registro sigue existiendo con `active = false` y queda
     * marcado como pendiente de sincronizar.
     *
     * @return número de filas actualizadas (0 si el id no existe).
     */
    @Query(
        """
        UPDATE service
        SET active = 0,
            sync_state = 'PENDIENTE',
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun softDelete(id: Long, updatedAt: Long): Int
}
