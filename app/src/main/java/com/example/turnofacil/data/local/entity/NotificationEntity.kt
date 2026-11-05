package com.example.turnofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.turnofacil.domain.model.TipoNotificacion

/**
 * Notificación programada para una cita (RF10).
 *
 * Solo se guarda el modelo; el envío real se implementará en una fase posterior.
 */
@Entity(
    tableName = "notification",
    foreignKeys = [
        ForeignKey(
            entity = AppointmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["appointment_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["appointment_id"])],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "appointment_id")
    val appointmentId: Long,
    val type: TipoNotificacion,
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Long,
    val message: String,
    val delivered: Boolean = false,
)
