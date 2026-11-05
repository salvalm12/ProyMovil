// Implementación en memoria usada por previews y como respaldo de la UI.
// La aplicación real usa data/repository/RepositoriosRoom.kt.
package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ServicioRepositorioDemo : ServicioRepositorio {
    private val _servicios = MutableStateFlow(DatosDemo.servicios)
    private val _adicionales = MutableStateFlow(
        DatosDemo.adicionales.map { Adicional(it.nombre, it.precio) },
    )

    override fun observarServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override fun observarAdicionales(): Flow<List<Adicional>> = _adicionales.asStateFlow()

    override suspend fun obtenerServicio(id: String): Servicio? {
        return _servicios.value.firstOrNull { it.id == id }
    }

    override fun observarTodosServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override suspend fun guardarServicio(servicio: Servicio): String {
        val id = servicio.id.ifBlank { UUID.randomUUID().toString() }
        val nuevo = servicio.copy(id = id)
        val indice = _servicios.value.indexOfFirst { it.id == id }
        _servicios.value = if (indice >= 0) {
            _servicios.value.toMutableList().also { it[indice] = nuevo }
        } else {
            _servicios.value + nuevo
        }
        return id
    }

    override suspend fun eliminarServicio(id: String): Boolean {
        val indice = _servicios.value.indexOfFirst { it.id == id }
        if (indice < 0) return false
        _servicios.value = _servicios.value.toMutableList().also {
            it[indice] = it[indice].copy(activo = false)
        }
        return true
    }
}

class CitaRepositorioDemo : CitaRepositorio {
    private val _citas = MutableStateFlow(DatosDemo.citas)

    override suspend fun horarioOcupado(fecha: String, hora: String): Boolean {
        // Regla 1: Dos citas PENDIENTE o CONFIRMADA no pueden compartir fecha + hora.
        // CANCELADA libera el horario.
        return _citas.value.any { cita ->
            cita.fecha == fecha &&
                cita.hora == hora &&
                (cita.estado == EstadoCita.PENDIENTE || cita.estado == EstadoCita.CONFIRMADA)
        }
    }

    override suspend fun agendar(cita: Cita): ResultadoAgendar {
        if (horarioOcupado(cita.fecha, cita.hora)) {
            return ResultadoAgendar.HorarioOcupado
        }
        // Regla 2: Una cita nueva siempre nace PENDIENTE
        val nuevaCita = cita.copy(
            id = cita.id.ifEmpty { UUID.randomUUID().toString() },
            estado = EstadoCita.PENDIENTE,
        )
        _citas.value = _citas.value + nuevaCita
        return ResultadoAgendar.Exito
    }

    override fun observarCitas(fecha: String): Flow<List<Cita>> =
        _citas.map { lista -> lista.filter { it.fecha == fecha } }

    override fun observarCita(id: String): Flow<Cita?> =
        _citas.map { lista -> lista.firstOrNull { it.id == id } }

    override suspend fun confirmar(id: String): Boolean =
        cambiarEstado(id, EstadoCita.CONFIRMADA)

    override suspend fun cancelar(id: String): Boolean =
        cambiarEstado(id, EstadoCita.CANCELADA)

    private fun cambiarEstado(id: String, estado: EstadoCita): Boolean {
        val indice = _citas.value.indexOfFirst { it.id == id }
        if (indice < 0) return false
        _citas.value = _citas.value.toMutableList().also {
            it[indice] = it[indice].copy(estado = estado)
        }
        return true
    }
}
