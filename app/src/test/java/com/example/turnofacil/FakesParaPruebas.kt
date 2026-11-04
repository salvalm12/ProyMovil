package com.example.turnofacil

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.viewmodel.Adicional
import com.example.turnofacil.ui.viewmodel.CitaRepositorio
import com.example.turnofacil.ui.viewmodel.ResultadoAgendar
import com.example.turnofacil.ui.viewmodel.ServicioRepositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeServicioRepositorio(
    serviciosIniciales: List<Servicio> = emptyList(),
    adicionalesIniciales: List<Adicional> = emptyList(),
) : ServicioRepositorio {

    private val _servicios = MutableStateFlow(serviciosIniciales)
    private val _adicionales = MutableStateFlow(adicionalesIniciales)

    override fun observarServicios(): Flow<List<Servicio>> = _servicios.asStateFlow()

    override fun observarAdicionales(): Flow<List<Adicional>> = _adicionales.asStateFlow()

    override suspend fun obtenerServicio(id: String): Servicio? {
        return _servicios.value.firstOrNull { it.id == id }
    }
}

class FakeCitaRepositorio(
    citasIniciales: List<Cita> = emptyList(),
) : CitaRepositorio {

    val citas = citasIniciales.toMutableList()

    override suspend fun horarioOcupado(fecha: String, hora: String): Boolean {
        // Regla 1: Dos citas PENDIENTE o CONFIRMADA no pueden compartirlo; CANCELADA libera el horario.
        return citas.any { cita ->
            cita.fecha == fecha &&
                cita.hora == hora &&
                (cita.estado == EstadoCita.PENDIENTE || cita.estado == EstadoCita.CONFIRMADA)
        }
    }

    override suspend fun agendar(cita: Cita): ResultadoAgendar {
        if (horarioOcupado(cita.fecha, cita.hora)) {
            return ResultadoAgendar.HorarioOcupado
        }
        val nuevaCita = cita.copy(
            id = if (cita.id.isEmpty()) "c_${citas.size + 1}" else cita.id,
            estado = EstadoCita.PENDIENTE,
        )
        citas.add(nuevaCita)
        return ResultadoAgendar.Exito
    }
}
