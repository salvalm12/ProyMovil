package com.example.turnofacil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Adicional que puede sumarse a una cita (por ejemplo, lavado o barba). */
@Entity(tableName = "addon")
data class AddonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val active: Boolean = true,
)
