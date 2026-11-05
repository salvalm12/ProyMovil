package com.example.turnofacil.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.turnofacil.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a notificaciones (RF10).
 *
 * Solo modelo y DAO; la programación/envío real se hará en una fase posterior.
 */
@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notification WHERE id = :id")
    suspend fun getById(id: Long): NotificationEntity?

    @Query(
        """
        SELECT * FROM notification
        WHERE appointment_id = :appointmentId
        ORDER BY scheduled_at
        """,
    )
    fun observeByAppointment(appointmentId: Long): Flow<List<NotificationEntity>>

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("DELETE FROM notification WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
