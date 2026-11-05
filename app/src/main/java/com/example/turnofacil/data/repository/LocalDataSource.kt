package com.example.turnofacil.data.repository

import androidx.room.withTransaction
import com.example.turnofacil.data.local.TurnoDatabase
import com.example.turnofacil.data.local.dao.AddonDao
import com.example.turnofacil.data.local.dao.AppointmentDao
import com.example.turnofacil.data.local.dao.ServiceDao
import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.ServiceEntity
import com.example.turnofacil.data.local.relation.AppointmentWithDetails
import com.example.turnofacil.domain.model.EstadoCita
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Envoltorio delgado sobre los DAOs y la base de datos de Room.
 * Proporciona un único punto de acceso local sin lógica de negocio.
 */
open class LocalDataSource(
    private val database: TurnoDatabase? = null,
    private val serviceDao: ServiceDao? = database?.serviceDao(),
    private val appointmentDao: AppointmentDao? = database?.appointmentDao(),
    private val addonDao: AddonDao? = database?.addonDao(),
) {
    // --- Servicios ---
    open fun observeAllServices(): Flow<List<ServiceEntity>> = serviceDao?.observeAll() ?: error("DAO no disponible")

    open fun observeActiveServices(): Flow<List<ServiceEntity>> = serviceDao?.observeActive() ?: error("DAO no disponible")

    open suspend fun getServiceById(id: Long): ServiceEntity? = serviceDao?.getById(id)

    open suspend fun getServiceByRemoteId(remoteId: String): ServiceEntity? = serviceDao?.getByRemoteId(remoteId)

    open suspend fun insertService(service: ServiceEntity): Long = serviceDao?.insert(service) ?: 0L

    open suspend fun updateService(service: ServiceEntity): Int = serviceDao?.update(service) ?: 0

    open suspend fun softDeleteService(id: Long, updatedAt: Long): Int = serviceDao?.softDelete(id, updatedAt) ?: 0

    // --- Citas ---
    open fun observeAppointmentsWithDetailsByDate(date: LocalDate): Flow<List<AppointmentWithDetails>> =
        appointmentDao?.observeWithDetailsByDate(date) ?: error("DAO no disponible")

    open fun observeAppointmentWithDetailsById(id: Long): Flow<AppointmentWithDetails?> =
        appointmentDao?.observeWithDetailsById(id) ?: error("DAO no disponible")

    open suspend fun getAppointmentWithDetailsById(id: Long): AppointmentWithDetails? =
        appointmentDao?.getWithDetailsById(id)

    open suspend fun getAppointmentById(id: Long): AppointmentEntity? = appointmentDao?.getById(id)

    open suspend fun insertAppointment(appointment: AppointmentEntity): Long =
        appointmentDao?.insert(appointment) ?: 0L

    open suspend fun insertAppointmentAddonCrossRefs(crossRefs: List<AppointmentAddonCrossRef>) {
        appointmentDao?.insertAddonCrossRefs(crossRefs)
    }

    open suspend fun updateAppointmentStatus(id: Long, status: EstadoCita, updatedAt: Long): Int =
        appointmentDao?.updateStatus(id, status, updatedAt) ?: 0

    open suspend fun rescheduleAppointment(id: Long, date: LocalDate, time: LocalTime, updatedAt: Long): Int =
        appointmentDao?.reschedule(id, date, time, updatedAt) ?: 0

    open suspend fun cancelAppointment(id: Long, updatedAt: Long): Int =
        appointmentDao?.cancel(id, updatedAt) ?: 0

    open suspend fun isSlotBusy(date: LocalDate, time: LocalTime): Boolean =
        appointmentDao?.isSlotBusy(date, time) ?: false

    open suspend fun isSlotBusyExcluding(date: LocalDate, time: LocalTime, excludeId: Long): Boolean =
        appointmentDao?.isSlotBusyExcluding(date, time, excludeId) ?: false

    // --- Adicionales ---
    open fun observeActiveAddons(): Flow<List<AddonEntity>> = addonDao?.observeActive() ?: error("DAO no disponible")

    open suspend fun getAddonByName(name: String): AddonEntity? = addonDao?.getByName(name)

    open suspend fun getAddonById(id: Long): AddonEntity? = addonDao?.getById(id)

    // --- Transacciones ---
    open suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database?.withTransaction(block) ?: block()
    }
}
