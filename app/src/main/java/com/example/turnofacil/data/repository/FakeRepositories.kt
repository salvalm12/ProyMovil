package com.example.turnofacil.data.repository

import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [ServiceRepository] utilizando [MutableStateFlow].
 */
class FakeServiceRepository : ServiceRepository {
    private val _servicios = MutableStateFlow<List<Servicio>>(DatosDemo.servicios)

    override fun getServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override suspend fun getServicioPorId(id: String): Servicio? {
        return _servicios.value.firstOrNull { it.id == id }
    }

    override suspend fun agregarServicio(servicio: Servicio) {
        _servicios.value = _servicios.value + servicio
    }

    override suspend fun actualizarServicio(servicio: Servicio) {
        _servicios.value = _servicios.value.map {
            if (it.id == servicio.id) servicio else it
        }
    }

    override suspend fun eliminarServicio(id: String): Boolean {
        val servicioExistente = getServicioPorId(id) ?: return false
        // Soft delete / baja lógica:
        _servicios.value = _servicios.value.map {
            if (it.id == id) it.copy(activo = false) else it
        }
        return true
    }
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
    ): Result<Unit> {
        val citaActual = getCitaPorId(id) ?: return Result.failure(Exception("Cita no encontrada"))

        // Regla 1: Una cita ocupa un horario (fecha + hora). Dos citas Pendientes o Confirmadas no pueden compartirlo.
        val horarioOcupado = _citas.value.any { otra ->
            otra.id != id &&
                    otra.fecha.equals(nuevaFecha, ignoreCase = true) &&
                    otra.hora == nuevaHora &&
                    (otra.estado == EstadoCita.PENDIENTE || otra.estado == EstadoCita.CONFIRMADA)
        }

        if (horarioOcupado) {
            return Result.failure(Exception("El horario $nuevaHora en la fecha $nuevaFecha ya está ocupado por otra cita activa."))
        }

        _citas.value = _citas.value.map { cita ->
            if (cita.id == id) {
                cita.copy(fecha = nuevaFecha, hora = nuevaHora)
            } else {
                cita
            }
        }
        return Result.success(Unit)
    }

    override suspend fun agregarCita(cita: Cita): Result<Unit> {
        // Regla 1: Validar colisión de horarios
        val horarioOcupado = _citas.value.any { otra ->
            otra.fecha.equals(cita.fecha, ignoreCase = true) &&
                    otra.hora == cita.hora &&
                    (otra.estado == EstadoCita.PENDIENTE || otra.estado == EstadoCita.CONFIRMADA)
        }
        if (horarioOcupado) {
            return Result.failure(Exception("Horario ocupado"))
        }

        // Regla 2: Una cita nueva siempre nace Pendiente
        val nuevaCita = cita.copy(estado = EstadoCita.PENDIENTE)
        _citas.value = _citas.value + nuevaCita
        return Result.success(Unit)
    }

    override fun tieneCitasAsociadasAServicio(servicioId: String): Boolean {
        return _citas.value.any { it.servicioId == servicioId }
    }

    override fun estaSincronizado(): Flow<Boolean> = _sincronizado.asStateFlow()
}
