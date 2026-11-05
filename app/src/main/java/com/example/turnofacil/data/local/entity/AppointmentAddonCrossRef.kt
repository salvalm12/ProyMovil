package com.example.turnofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla cruzada N-M entre citas y adicionales. */
@Entity(
    tableName = "appointment_addon_cross_ref",
    primaryKeys = ["appointment_id", "addon_id"],
    foreignKeys = [
        ForeignKey(
            entity = AppointmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["appointment_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AddonEntity::class,
            parentColumns = ["id"],
            childColumns = ["addon_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["addon_id"])],
)
data class AppointmentAddonCrossRef(
    @ColumnInfo(name = "appointment_id")
    val appointmentId: Long,
    @ColumnInfo(name = "addon_id")
    val addonId: Long,
)
