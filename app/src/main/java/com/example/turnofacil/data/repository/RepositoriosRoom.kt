package com.example.turnofacil.data.repository

import androidx.room.withTransaction
import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.local.TurnoDatabase
import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.ServiceEntity
import com.example.turnofacil.data.local.relation.AppointmentWithDetails
import com.example.turnofacil.domain.model.Cita
import com.example.turnofacil.domain.model.EstadoCita
import com.example.turnofacil.domain.model.Servicio
import com.example.turnofacil.domain.model.SyncState
import com.example.turnofacil.ui.viewmodel.Adicional
import com.example.turnofacil.ui.viewmodel.CitaRepositorio
import com.example.turnofacil.ui.viewmodel.ResultadoAgendar
import com.example.turnofacil.ui.viewmodel.ServicioRepositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

/**
 * Implementación de los contratos de UI sobre Room.
 *
 * La UI sigue trabajando con [Servicio] y [Cita]; estas clases se encargan de
 * convertir a/desde las entidades locales y de aplicar las reglas de negocio.
 */
class ServicioRepositorioRoom(
    private val database: TurnoDatabase,
) : ServicioRepositorio {

    private val serviceDao = database.serviceDao()
    private val addonDao = database.addonDao()

    override fun observarServicios(): Flow<List<Servicio>> =
        serviceDao.observeActive().map { lista -> lista.map { it.aDominio() } }

    override fun observarAdicionales(): Flow<List<Adicional>> =
        addonDao.observeActive().map { lista -> lista.map { it.aAdicional() } }

    override suspend fun obtenerServicio(id: String): Servicio? =
        id.toLongOrNull()?.let { serviceDao.getById(it) }?.aDominio()

    override fun observarTodosServicios(): Flow<List<Servicio>> =
        serviceDao.observeAll().map { lista -> lista.map(ServiceEntity::aDominio) }

    override suspend fun guardarServicio(servicio: Servicio): String {
        val idLocal = servicio.id.toLongOrNull()
        val existente = idLocal?.let { serviceDao.getById(it) }
        val ahora = System.currentTimeMillis()
        val entidad = if (existente != null) {
            existente.copy(
                name = servicio.nombre,
                price = servicio.precio,
                durationMin = servicio.duracionMin,
                active = servicio.activo,
                syncState = SyncState.PENDIENTE,
                updatedAt = ahora,
            )
        } else {
            ServiceEntity(
                remoteId = servicio.id.takeIf { it.isNotBlank() && idLocal == null },
                name = servicio.nombre,
                price = servicio.precio,
                durationMin = servicio.duracionMin,
                active = servicio.activo,
                syncState = SyncState.PENDIENTE,
                updatedAt = ahora,
            )
        }

        return if (existente != null) {
            serviceDao.update(entidad)
            entidad.id.toString()
        } else {
            serviceDao.insert(entidad).toString()
        }
    }

    override suspend fun eliminarServicio(id: String): Boolean {
        val idLocal = id.toLongOrNull() ?: return false
        return serviceDao.softDelete(idLocal, System.currentTimeMillis()) > 0
    }
}

class CitaRepositorioRoom(
    private val database: TurnoDatabase,
) : CitaRepositorio {

    private val appointmentDao = database.appointmentDao()
    private val serviceDao = database.serviceDao()
    private val addonDao = database.addonDao()

    override suspend fun horarioOcupado(fecha: String, hora: String): Boolean {
        val date = fechaLocal(fecha) ?: return false
        val time = horaLocal(hora) ?: return false
        return appointmentDao.isSlotBusy(date, time)
    }

    override suspend fun agendar(cita: Cita): ResultadoAgendar {
        val date = fechaLocal(cita.fecha) ?: return ResultadoAgendar.HorarioOcupado
        val time = horaLocal(cita.hora) ?: return ResultadoAgendar.HorarioOcupado
        val serviceId = cita.servicioId.toLongOrNull()
            ?: return ResultadoAgendar.HorarioOcupado

        return database.withTransaction {
            if (appointmentDao.isSlotBusy(date, time)) {
                return@withTransaction ResultadoAgendar.HorarioOcupado
            }

            val servicio = serviceDao.getById(serviceId)
                ?: return@withTransaction ResultadoAgendar.HorarioOcupado
            val adicionales = cita.adicionales.mapNotNull { addonDao.getByName(it) }
            val total = servicio.price + adicionales.sumOf { it.price }

            val appointmentId = appointmentDao.insert(
                AppointmentEntity(
                    serviceId = serviceId,
                    customerName = cita.cliente,
                    attentionType = cita.tipoAtencion,
                    date = date,
                    time = time,
                    status = EstadoCita.PENDIENTE,
                    totalPrice = total,
                    syncState = SyncState.PENDIENTE,
                ),
            )

            if (adicionales.isNotEmpty()) {
                appointmentDao.insertAddonCrossRefs(
                    adicionales.map { adicional ->
                        AppointmentAddonCrossRef(
                            appointmentId = appointmentId,
                            addonId = adicional.id,
                        )
                    },
                )
            }

            ResultadoAgendar.Exito
        }
    }

    override fun observarCitas(fecha: String): Flow<List<Cita>> {
        val date = fechaLocal(fecha) ?: LocalDate.now()
        return appointmentDao.observeWithDetailsByDate(date)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override fun observarCita(id: String): Flow<Cita?> {
        val idLocal = id.toLongOrNull() ?: return flowOf(null)
        return appointmentDao.observeWithDetailsById(idLocal)
            .map { detalle -> detalle?.aDominio() }
    }

    override suspend fun confirmar(id: String): Boolean {
        val idLocal = id.toLongOrNull() ?: return false
        return appointmentDao.updateStatus(
            id = idLocal,
            status = EstadoCita.CONFIRMADA,
            updatedAt = System.currentTimeMillis(),
        ) > 0
    }

    override suspend fun cancelar(id: String): Boolean {
        val idLocal = id.toLongOrNull() ?: return false
        return appointmentDao.cancel(idLocal, System.currentTimeMillis()) > 0
    }
}

/**
 * Carga una sola vez los datos de demostración en Room.
 *
 * Se usa mientras no existe todavía la sincronización con la API. Si la tabla
 * de servicios ya tiene filas, no vuelve a sembrar nada.
 */
class RoomSeeder(
    private val database: TurnoDatabase,
) {

    suspend fun seedIfEmpty() {
        database.withTransaction {
            if (database.serviceDao().count() > 0) return@withTransaction

            val servicios = mutableMapOf<String, Long>()
            DatosDemo.servicios.forEach { servicio ->
                servicios[servicio.id] = database.serviceDao().insert(
                    ServiceEntity(
                        remoteId = servicio.id,
                        name = servicio.nombre,
                        price = servicio.precio,
                        durationMin = servicio.duracionMin,
                        active = servicio.activo,
                        syncState = SyncState.SINCRONIZADO,
                    ),
                )
            }

            val adicionales = mutableMapOf<String, Long>()
            DatosDemo.adicionales.forEach { adicional ->
                adicionales[adicional.nombre] = database.addonDao().insert(
                    AddonEntity(
                        name = adicional.nombre,
                        price = adicional.precio,
                    ),
                )
            }

            val hoy = LocalDate.now()
            DatosDemo.citas.forEach { cita ->
                val servicioId = servicios[cita.servicioId] ?: return@forEach
                val servicio = DatosDemo.servicios.firstOrNull { it.id == cita.servicioId }
                val total = (servicio?.precio ?: 0.0) + cita.adicionales.sumOf { nombre ->
                    DatosDemo.adicionales.firstOrNull { it.nombre == nombre }?.precio ?: 0.0
                }

                val appointmentId = database.appointmentDao().insert(
                    AppointmentEntity(
                        serviceId = servicioId,
                        remoteId = cita.id,
                        customerName = cita.cliente,
                        attentionType = cita.tipoAtencion,
                        date = hoy,
                        time = LocalTime.parse(cita.hora),
                        status = cita.estado,
                        totalPrice = total,
                        syncState = SyncState.SINCRONIZADO,
                    ),
                )

                val crossRefs = cita.adicionales.mapNotNull { nombre ->
                    adicionales[nombre]?.let { addonId ->
                        AppointmentAddonCrossRef(
                            appointmentId = appointmentId,
                            addonId = addonId,
                        )
                    }
                }
                if (crossRefs.isNotEmpty()) {
                    database.appointmentDao().insertAddonCrossRefs(crossRefs)
                }
            }
        }
    }
}

private fun ServiceEntity.aDominio(): Servicio = Servicio(
    id = id.toString(),
    nombre = name,
    precio = price,
    duracionMin = durationMin,
    activo = active,
)

private fun AddonEntity.aAdicional(): Adicional = Adicional(
    nombre = name,
    precio = price,
)

private fun AppointmentWithDetails.aDominio(): Cita = Cita(
    id = appointment.id.toString(),
    cliente = appointment.customerName,
    servicioId = appointment.serviceId.toString(),
    tipoAtencion = appointment.attentionType,
    adicionales = addons.map { it.name },
    hora = appointment.time.toString(),
    fecha = if (appointment.date == LocalDate.now()) "Hoy" else appointment.date.toString(),
    estado = appointment.status,
    nota = "",
)

private fun fechaLocal(fecha: String): LocalDate? {
    val texto = fecha.trim()
    return when {
        texto.isEmpty() || texto.equals("Hoy", ignoreCase = true) -> LocalDate.now()
        else -> runCatching { LocalDate.parse(texto) }.getOrNull()
    }
}

private fun horaLocal(hora: String): LocalTime? =
    runCatching { LocalTime.parse(hora.trim()) }.getOrNull()
