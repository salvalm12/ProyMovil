package com.example.turnofacil.data.repository


import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.relation.AppointmentWithDetails
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

/**
 * Implementación de [AppointmentRepository] sobre [LocalDataSource].
 * Aplica reglas de negocio (horario único, estado inicial Pendiente, cálculo de total en Room, liberación de horario).
 */
class AppointmentRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : AppointmentRepository {

    private val _syncState = MutableStateFlow(true)

    override fun getCitas(): Flow<List<Cita>> {
        val hoy = LocalDate.now()
        return localDataSource.observeAppointmentsWithDetailsByDate(hoy).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    override fun getCitasPorFecha(fecha: String): Flow<List<Cita>> {
        val date = parseFecha(fecha) ?: LocalDate.now()
        return localDataSource.observeAppointmentsWithDetailsByDate(date).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    override suspend fun getCitaPorId(id: String): Cita? {
        val idLong = id.toLongOrNull() ?: return null
        return localDataSource.getAppointmentWithDetailsById(idLong)?.toDomain()
    }

    override fun observarCitaPorId(id: String): Flow<Cita?> {
        val idLong = id.toLongOrNull() ?: return flowOf(null)
        return localDataSource.observeAppointmentWithDetailsById(idLong).map { it?.toDomain() }
    }

    override suspend fun agendarCita(solicitud: SolicitudAgendarCita): ResultadoAgendar {
        val date = parseFecha(solicitud.fecha) ?: return ResultadoAgendar.Error("Fecha inválida")
        val time = parseHora(solicitud.hora) ?: return ResultadoAgendar.Error("Hora inválida")
        val serviceIdLong = solicitud.servicioId.toLongOrNull()
            ?: return ResultadoAgendar.Error("Servicio no encontrado")

        return localDataSource.runInTransaction {
            // Regla 1: Validar colisión de horarios (solo citas PENDIENTE o CONFIRMADA ocupan el horario)
            if (localDataSource.isSlotBusy(date, time)) {
                return@runInTransaction ResultadoAgendar.HorarioOcupado
            }

            // Regla 4: Total = precio del servicio + precio de adicionales seleccionados (calculado en Room)
            val servicio = localDataSource.getServiceById(serviceIdLong)
                ?: return@runInTransaction ResultadoAgendar.Error("Servicio no disponible")

            val adicionales = solicitud.adicionales.mapNotNull { nombreAdicional ->
                localDataSource.getAddonByName(nombreAdicional)
            }
            val totalPrice = servicio.price + adicionales.sumOf { it.price }

            // Regla 2: Una cita nueva siempre nace PENDIENTE
            val ahora = System.currentTimeMillis()
            val appointmentId = localDataSource.insertAppointment(
                AppointmentEntity(
                    serviceId = serviceIdLong,
                    customerName = solicitud.cliente,
                    attentionType = solicitud.tipoAtencion,
                    date = date,
                    time = time,
                    status = EstadoCita.PENDIENTE,
                    totalPrice = totalPrice,
                    syncState = SyncState.PENDIENTE,
                    updatedAt = ahora,
                )
            )

            if (adicionales.isNotEmpty()) {
                val crossRefs = adicionales.map { addon ->
                    AppointmentAddonCrossRef(
                        appointmentId = appointmentId,
                        addonId = addon.id,
                    )
                }
                localDataSource.insertAppointmentAddonCrossRefs(crossRefs)
            }

            ResultadoAgendar.Exito
        }
    }

    override suspend fun agregarCita(cita: Cita): ResultadoAgendar {
        return agendarCita(
            SolicitudAgendarCita(
                servicioId = cita.servicioId,
                cliente = cita.cliente,
                tipoAtencion = cita.tipoAtencion,
                adicionales = cita.adicionales,
                hora = cita.hora,
                fecha = cita.fecha,
                nota = cita.nota,
            )
        )
    }

    override suspend fun confirmarCita(id: String): Boolean {
        return cambiarEstadoCita(id, EstadoCita.CONFIRMADA)
    }

    override suspend fun cancelarCita(id: String): Boolean {
        // Regla 3: Cancelar una cita la marca CANCELADA y libera el horario
        val idLong = id.toLongOrNull() ?: return false
        val ahora = System.currentTimeMillis()
        return localDataSource.cancelAppointment(idLong, ahora) > 0
    }

    override suspend fun cambiarEstadoCita(id: String, nuevoEstado: EstadoCita): Boolean {
        val idLong = id.toLongOrNull() ?: return false
        val ahora = System.currentTimeMillis()
        return localDataSource.updateAppointmentStatus(idLong, nuevoEstado, ahora) > 0
    }

    override suspend fun reprogramarCita(
        id: String,
        nuevaFecha: String,
        nuevaHora: String,
    ): ResultadoAgendar {
        val idLong = id.toLongOrNull() ?: return ResultadoAgendar.Error("Cita no encontrada")
        val newDate = parseFecha(nuevaFecha) ?: return ResultadoAgendar.Error("Fecha inválida")
        val newTime = parseHora(nuevaHora) ?: return ResultadoAgendar.Error("Hora inválida")

        // Regla 1: Validar colisión excluyendo la propia cita que se está reprogramando
        if (localDataSource.isSlotBusyExcluding(newDate, newTime, idLong)) {
            return ResultadoAgendar.HorarioOcupado
        }

        val ahora = System.currentTimeMillis()
        val count = localDataSource.rescheduleAppointment(idLong, newDate, newTime, ahora)
        return if (count > 0) ResultadoAgendar.Exito else ResultadoAgendar.Error("No se pudo reprogramar la cita")
    }

    override suspend fun esHorarioOcupado(fecha: String, hora: String): Boolean {
        val date = parseFecha(fecha) ?: return false
        val time = parseHora(hora) ?: return false
        return localDataSource.isSlotBusy(date, time)
    }

    override fun tieneCitasAsociadasAServicio(servicioId: String): Boolean {
        val serviceIdLong = servicioId.toLongOrNull() ?: return false
        return serviceIdLong > 0
    }

    override fun estaSincronizado(): Flow<Boolean> = _syncState.asStateFlow()
}

fun AppointmentWithDetails.toDomain(): Cita = Cita(
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

private fun parseFecha(fecha: String): LocalDate? {
    val text = fecha.trim()
    return when {
        text.isEmpty() || text.equals("Hoy", ignoreCase = true) -> LocalDate.now()
        else -> runCatching { LocalDate.parse(text) }.getOrNull()
    }
}

private fun parseHora(hora: String): LocalTime? {
    val text = hora.trim()
    return runCatching { LocalTime.parse(text) }.getOrNull()
}
