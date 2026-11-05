// TODO(temporal): reemplazar por los Repository reales de los Integrantes 1 y 2
package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ServicioRepositorioDemo : ServicioRepositorio {
    private val _servicios = MutableStateFlow(DatosDemo.servicios)
    private val adicionalesPredefinidos = listOf(
        Adicional("Lavado", 50.0),
        Adicional("Diseño de ceja", 60.0),
        Adicional("Tratamiento", 100.0),
        Adicional("Barba", 80.0),
    )
    private val _adicionales = MutableStateFlow(adicionalesPredefinidos)

    override fun observarServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override fun observarAdicionales(): Flow<List<Adicional>> = _adicionales.asStateFlow()

    override suspend fun obtenerServicio(id: String): Servicio? {
        return _servicios.value.firstOrNull { it.id == id }
    }
}

class CitaRepositorioDemo : CitaRepositorio {
    private val citasEnMemoria = DatosDemo.citas.toMutableList()

    override suspend fun horarioOcupado(fecha: String, hora: String): Boolean {
        // Regla 1: Dos citas PENDIENTE o CONFIRMADA no pueden compartir fecha + hora.
        // CANCELADA libera el horario.
        return citasEnMemoria.any { cita ->
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
        citasEnMemoria.add(nuevaCita)
        return ResultadoAgendar.Exito
    }
}
