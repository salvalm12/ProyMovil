package com.example.turnofacil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.relation.AppointmentWithDetails
import com.example.turnofacil.domain.model.EstadoCita
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Acceso a citas.
 *
 * Reglas de negocio implementadas aquí:
 * - una cita nueva nace [EstadoCita.PENDIENTE] (valor por defecto de la entidad);
 * - solo las citas Pendiente/Confirmada ocupan un horario;
 * - una cita Cancelada libera el horario.
 */
@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    /** Asocia un adicional a una cita (tabla cruzada N-M). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAddonCrossRef(crossRef: AppointmentAddonCrossRef)

    /** Asocia varios adicionales a una cita en una sola operación. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAddonCrossRefs(crossRefs: List<AppointmentAddonCrossRef>)

    @Query(
        """
        UPDATE appointment
        SET status = :status,
            sync_state = 'PENDIENTE',
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(id: Long, status: EstadoCita, updatedAt: Long): Int

    /** Reprograma fecha y hora; el repositorio valida antes el nuevo horario. */
    @Query(
        """
        UPDATE appointment
        SET date = :date,
            time = :time,
            sync_state = 'PENDIENTE',
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun reschedule(
        id: Long,
        date: LocalDate,
        time: LocalTime,
        updatedAt: Long,
    ): Int

    @Query("SELECT * FROM appointment WHERE id = :id")
    suspend fun getById(id: Long): AppointmentEntity?

    /** Cancelación: la cita sigue existiendo y su horario queda libre. */
    @Query(
        """
        UPDATE appointment
        SET status = 'CANCELADA',
            sync_state = 'PENDIENTE',
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun cancel(id: Long, updatedAt: Long): Int

    /** Agenda del día, ordenada por hora (CU3). */
    @Query("SELECT * FROM appointment WHERE date = :date ORDER BY time")
    fun observeByDate(date: LocalDate): Flow<List<AppointmentEntity>>

    /** Detalle de una cita con servicio y adicionales. */
    @Transaction
    @Query("SELECT * FROM appointment WHERE id = :id")
    suspend fun getWithDetailsById(id: Long): AppointmentWithDetails?

    @Transaction
    @Query("SELECT * FROM appointment WHERE id = :id")
    fun observeWithDetailsById(id: Long): Flow<AppointmentWithDetails?>

    /** Agenda del día con servicio y adicionales resueltos. */
    @Transaction
    @Query("SELECT * FROM appointment WHERE date = :date ORDER BY time")
    fun observeWithDetailsByDate(date: LocalDate): Flow<List<AppointmentWithDetails>>

    /**
     * ¿Ya hay una cita Pendiente o Confirmada en esa fecha y hora?
     *
     * Las citas Canceladas no cuentan, por lo que liberan el horario.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM appointment
            WHERE date = :date
              AND time = :time
              AND status IN ('PENDIENTE', 'CONFIRMADA')
        )
        """,
    )
    suspend fun isSlotBusy(date: LocalDate, time: LocalTime): Boolean

    /**
     * Igual que [isSlotBusy], pero ignora la cita que se está reprogramando.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM appointment
            WHERE date = :date
              AND time = :time
              AND status IN ('PENDIENTE', 'CONFIRMADA')
              AND id != :excludeId
        )
        """,
    )
    suspend fun isSlotBusyExcluding(
        date: LocalDate,
        time: LocalTime,
        excludeId: Long,
    ): Boolean
}
