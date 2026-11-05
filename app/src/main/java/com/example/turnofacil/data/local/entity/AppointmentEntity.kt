package com.example.turnofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.turnofacil.domain.model.EstadoCita
import com.example.turnofacil.domain.model.SyncState
import com.example.turnofacil.domain.model.TipoAtencion
import java.time.LocalDate
import java.time.LocalTime

/**
 * Cita agendada.
 *
 * `service_id` es llave foránea a [ServiceEntity]; el borrado del servicio se
 * restringe porque la regla de negocio exige baja lógica, no borrado físico.
 */
@Entity(
    tableName = "appointment",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["service_id"]),
        Index(value = ["date", "time"]),
    ],
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "service_id")
    val serviceId: Long,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "customer_name")
    val customerName: String,
    @ColumnInfo(name = "attention_type")
    val attentionType: TipoAtencion,
    val date: LocalDate,
    val time: LocalTime,
    val status: EstadoCita = EstadoCita.PENDIENTE,
    @ColumnInfo(name = "total_price")
    val totalPrice: Double,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.PENDIENTE,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
