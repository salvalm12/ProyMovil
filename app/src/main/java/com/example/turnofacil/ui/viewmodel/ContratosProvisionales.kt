// TODO(temporal): reemplazar por los Repository reales de los Integrantes 1 y 2
package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow

data class Adicional(
    val nombre: String,
    val precio: Double,
)

interface ServicioRepositorio {
    fun observarServicios(): Flow<List<Servicio>>
    fun observarAdicionales(): Flow<List<Adicional>>
    suspend fun obtenerServicio(id: String): Servicio?
}

sealed interface ResultadoAgendar {
    data object Exito : ResultadoAgendar
    data object HorarioOcupado : ResultadoAgendar
}

interface CitaRepositorio {
    suspend fun horarioOcupado(fecha: String, hora: String): Boolean
    suspend fun agendar(cita: Cita): ResultadoAgendar
}
