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
class LocalDataSource(
    private val database: TurnoDatabase,
    private val serviceDao: ServiceDao = database.serviceDao(),
    private val appointmentDao: AppointmentDao = database.appointmentDao(),
    private val addonDao: AddonDao = database.addonDao(),
) {
    // --- Servicios ---
    fun observeAllServices(): Flow<List<ServiceEntity>> = serviceDao.observeAll()

    fun observeActiveServices(): Flow<List<ServiceEntity>> = serviceDao.observeActive()

    suspend fun getServiceById(id: Long): ServiceEntity? = serviceDao.getById(id)

    suspend fun getServiceByRemoteId(remoteId: String): ServiceEntity? = serviceDao.getByRemoteId(remoteId)

    suspend fun insertService(service: ServiceEntity): Long = serviceDao.insert(service)

    suspend fun updateService(service: ServiceEntity): Int = serviceDao.update(service)

    suspend fun softDeleteService(id: Long, updatedAt: Long): Int = serviceDao.softDelete(id, updatedAt)

    // --- Citas ---
    fun observeAppointmentsWithDetailsByDate(date: LocalDate): Flow<List<AppointmentWithDetails>> =
        appointmentDao.observeWithDetailsByDate(date)

    fun observeAppointmentWithDetailsById(id: Long): Flow<AppointmentWithDetails?> =
        appointmentDao.observeWithDetailsById(id)

    suspend fun getAppointmentWithDetailsById(id: Long): AppointmentWithDetails? =
        appointmentDao.getWithDetailsById(id)

    suspend fun getAppointmentById(id: Long): AppointmentEntity? = appointmentDao.getById(id)

    suspend fun insertAppointment(appointment: AppointmentEntity): Long =
        appointmentDao.insert(appointment)

    suspend fun insertAppointmentAddonCrossRefs(crossRefs: List<AppointmentAddonCrossRef>) =
        appointmentDao.insertAddonCrossRefs(crossRefs)

    suspend fun updateAppointmentStatus(id: Long, status: EstadoCita, updatedAt: Long): Int =
        appointmentDao.updateStatus(id, status, updatedAt)

    suspend fun rescheduleAppointment(id: Long, date: LocalDate, time: LocalTime, updatedAt: Long): Int =
        appointmentDao.reschedule(id, date, time, updatedAt)

    suspend fun cancelAppointment(id: Long, updatedAt: Long): Int =
        appointmentDao.cancel(id, updatedAt)

    suspend fun isSlotBusy(date: LocalDate, time: LocalTime): Boolean =
        appointmentDao.isSlotBusy(date, time)

    suspend fun isSlotBusyExcluding(date: LocalDate, time: LocalTime, excludeId: Long): Boolean =
        appointmentDao.isSlotBusyExcluding(date, time, excludeId)

    // --- Adicionales ---
    fun observeActiveAddons(): Flow<List<AddonEntity>> = addonDao.observeActive()

    suspend fun getAddonByName(name: String): AddonEntity? = addonDao.getByName(name)

    suspend fun getAddonById(id: Long): AddonEntity? = addonDao.getById(id)

    // --- Transacciones ---
    suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withTransaction(block)
    }
}
