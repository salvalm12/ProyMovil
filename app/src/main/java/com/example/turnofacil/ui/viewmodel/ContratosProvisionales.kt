// Contratos usados por los ViewModels. La implementación real vive en
// data/repository/RepositoriosRoom.kt y usa Room como fuente de verdad.
package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class Adicional(
    val nombre: String,
    val precio: Double,
)

interface ServicioRepositorio {
    fun observarServicios(): Flow<List<Servicio>>
    fun observarAdicionales(): Flow<List<Adicional>>
    suspend fun obtenerServicio(id: String): Servicio?

    /** Todos los servicios, incluidos los inactivos (administración). */
    fun observarTodosServicios(): Flow<List<Servicio>> = observarServicios()

    /** Inserta o actualiza un servicio y devuelve su id local como texto. */
    suspend fun guardarServicio(servicio: Servicio): String = servicio.id

    /** Baja lógica: el servicio deja de estar activo, pero conserva su historial. */
    suspend fun eliminarServicio(id: String): Boolean = false
}

sealed interface ResultadoAgendar {
    data object Exito : ResultadoAgendar
    data object HorarioOcupado : ResultadoAgendar
}

interface CitaRepositorio {
    suspend fun horarioOcupado(fecha: String, hora: String): Boolean
    suspend fun agendar(cita: Cita): ResultadoAgendar

    /** Agenda de una fecha ("Hoy" o ISO-8601). */
    fun observarCitas(fecha: String): Flow<List<Cita>> = flowOf(emptyList())

    /** Detalle observable de una cita por su id local. */
    fun observarCita(id: String): Flow<Cita?> = flowOf(null)

    suspend fun confirmar(id: String): Boolean = false
    suspend fun cancelar(id: String): Boolean = false
}
