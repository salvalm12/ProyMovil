package com.example.turnofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.turnofacil.domain.model.SyncState

/**
 * Servicio ofrecido por el negocio.
 *
 * La baja es lógica (`active = false`) para no perder el historial de citas.
 */
@Entity(tableName = "service")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    val name: String,
    val price: Double,
    @ColumnInfo(name = "duration_min")
    val durationMin: Int,
    val active: Boolean = true,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.PENDIENTE,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
