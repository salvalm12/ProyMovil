package com.example.turnofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.turnofacil.domain.model.TipoPropietarioMedia

/**
 * Archivo multimedia asociado a un servicio o a una cita (RF11).
 *
 * No hay llave foránea real porque el dueño puede ser de dos tablas distintas;
 * se usa `owner_type` + `owner_id`.
 */
@Entity(
    tableName = "media",
    indices = [Index(value = ["owner_type", "owner_id"])],
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "owner_type")
    val ownerType: TipoPropietarioMedia,
    @ColumnInfo(name = "owner_id")
    val ownerId: Long,
    val uri: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
