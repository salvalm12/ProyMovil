package com.example.turnofacil.data.repository

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para la gestión de citas/turnos.
 */
interface AppointmentRepository {
    fun getCitas(): Flow<List<Cita>>
    fun getCitasPorFecha(fecha: String): Flow<List<Cita>>
    suspend fun getCitaPorId(id: String): Cita?
    suspend fun cambiarEstadoCita(id: String, nuevoEstado: EstadoCita): Boolean
    suspend fun reprogramarCita(id: String, nuevaFecha: String, nuevaHora: String): Result<Unit>
    suspend fun agregarCita(cita: Cita): Result<Unit>
    fun tieneCitasAsociadasAServicio(servicioId: String): Boolean
    fun estaSincronizado(): Flow<Boolean>
}
