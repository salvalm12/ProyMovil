package com.example.turnofacil.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.ServiceEntity

/**
 * Cita con su servicio y sus adicionales resueltos.
 *
 * Se usa para el detalle de cita y para la agenda del día, evitando consultas
 * N+1 desde la UI.
 */
data class AppointmentWithDetails(
    @Embedded
    val appointment: AppointmentEntity,
    @Relation(
        parentColumn = "service_id",
        entityColumn = "id",
    )
    val service: ServiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AppointmentAddonCrossRef::class,
            parentColumn = "appointment_id",
            entityColumn = "addon_id",
        ),
    )
    val addons: List<AddonEntity>,
)
