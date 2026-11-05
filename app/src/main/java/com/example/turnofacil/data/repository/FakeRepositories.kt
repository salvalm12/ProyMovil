package com.example.turnofacil.data.repository

import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementación en memoria de [ServiceRepository] utilizando [MutableStateFlow].
 */
class FakeServiceRepository : ServiceRepository {
    private val _servicios = MutableStateFlow<List<Servicio>>(DatosDemo.servicios)
    private val _adicionales = MutableStateFlow<List<Adicional>>(
        DatosDemo.adicionales.mapIndexed { idx, item ->
            Adicional(id = (idx + 1).toString(), nombre = item.nombre, precio = item.precio)
        }
    )

    override fun getServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override fun getServiciosActivos(): Flow<List<Servicio>> {
        return _servicios.map { lista -> lista.filter { it.activo } }
    }

    override suspend fun getServicioPorId(id: String): Servicio? {
        return _servicios.value.firstOrNull { it.id == id }
    }

    override suspend fun agregarServicio(servicio: Servicio): String {
        val nuevoId = servicio.id.ifBlank { UUID.randomUUID().toString() }
        val nuevo = servicio.copy(id = nuevoId)
        _servicios.value = _servicios.value + nuevo
        return nuevoId
    }

    override suspend fun actualizarServicio(servicio: Servicio) {
        _servicios.value = _servicios.value.map {
            if (it.id == servicio.id) servicio else it
        }
    }

    override suspend fun eliminarServicio(id: String): Boolean {
        val servicioExistente = getServicioPorId(id) ?: return false
        _servicios.value = _servicios.value.map {
            if (it.id == id) it.copy(activo = false) else it
        }
        return true
    }

    override fun getAdicionales(): Flow<List<Adicional>> = _adicionales.asStateFlow()
}

/**
 * Implementación en memoria de [AppointmentRepository] con validaciones de reglas de negocio.
 */
class FakeAppointmentRepository(
    private val serviceRepository: ServiceRepository = FakeServiceRepository()
) : AppointmentRepository {

    private val _citas = MutableStateFlow<List<Cita>>(DatosDemo.citas)
    private val _sincronizado = MutableStateFlow(true)

    override fun getCitas(): Flow<List<Cita>> = _citas.asStateFlow()

    override fun getCitasPorFecha(fecha: String): Flow<List<Cita>> {
        return _citas.map { lista ->
            lista.filter { it.fecha.equals(fecha, ignoreCase = true) || fecha.equals("Hoy", ignoreCase = true) }
        }
    }

    override suspend fun getCitaPorId(id: String): Cita? {
        return _citas.value.firstOrNull { it.id == id }
    }

    override fun observarCitaPorId(id: String): Flow<Cita?> {
        return _citas.map { lista -> lista.firstOrNull { it.id == id } }
    }

    override suspend fun agendarCita(solicitud: SolicitudAgendarCita): ResultadoAgendar {
        if (esHorarioOcupado(solicitud.fecha, solicitud.hora)) {
            return ResultadoAgendar.HorarioOcupado
        }
        val nuevaCita = Cita(
            id = UUID.randomUUID().toString(),
            cliente = solicitud.cliente,
            servicioId = solicitud.servicioId,
            tipoAtencion = solicitud.tipoAtencion,
            adicionales = solicitud.adicionales,
            hora = solicitud.hora,
            fecha = solicitud.fecha,
            estado = EstadoCita.PENDIENTE,
            nota = solicitud.nota,
        )
        _citas.value = _citas.value + nuevaCita
        return ResultadoAgendar.Exito
    }

    override suspend fun agregarCita(cita: Cita): ResultadoAgendar {
        if (esHorarioOcupado(cita.fecha, cita.hora)) {
            return ResultadoAgendar.HorarioOcupado
        }
        val nuevaCita = cita.copy(
            id = cita.id.ifBlank { UUID.randomUUID().toString() },
            estado = EstadoCita.PENDIENTE,
        )
        _citas.value = _citas.value + nuevaCita
        return ResultadoAgendar.Exito
    }

    override suspend fun confirmarCita(id: String): Boolean {
        return cambiarEstadoCita(id, EstadoCita.CONFIRMADA)
    }

    override suspend fun cancelarCita(id: String): Boolean {
        return cambiarEstadoCita(id, EstadoCita.CANCELADA)
    }

    override suspend fun cambiarEstadoCita(id: String, nuevoEstado: EstadoCita): Boolean {
        var actualizado = false
        _citas.value = _citas.value.map { cita ->
            if (cita.id == id) {
                actualizado = true
                cita.copy(estado = nuevoEstado)
            } else {
                cita
            }
        }
        return actualizado
    }

    override suspend fun reprogramarCita(
        id: String,
        nuevaFecha: String,
        nuevaHora: String,
    ): ResultadoAgendar {
        val citaActual = getCitaPorId(id) ?: return ResultadoAgendar.Error("Cita no encontrada")

        val ocupado = _citas.value.any { otra ->
            otra.id != id &&
                    otra.fecha.equals(nuevaFecha, ignoreCase = true) &&
                    otra.hora == nuevaHora &&
                    (otra.estado == EstadoCita.PENDIENTE || otra.estado == EstadoCita.CONFIRMADA)
        }

        if (ocupado) {
            return ResultadoAgendar.HorarioOcupado
        }

        _citas.value = _citas.value.map { cita ->
            if (cita.id == id) {
                cita.copy(fecha = nuevaFecha, hora = nuevaHora)
            } else {
                cita
            }
        }
        return ResultadoAgendar.Exito
    }

    override suspend fun esHorarioOcupado(fecha: String, hora: String): Boolean {
        return _citas.value.any { cita ->
            cita.fecha.equals(fecha, ignoreCase = true) &&
                    cita.hora == hora &&
                    (cita.estado == EstadoCita.PENDIENTE || cita.estado == EstadoCita.CONFIRMADA)
        }
    }

    override fun tieneCitasAsociadasAServicio(servicioId: String): Boolean {
        return _citas.value.any { it.servicioId == servicioId }
    }

    override fun estaSincronizado(): Flow<Boolean> = _sincronizado.asStateFlow()
}
