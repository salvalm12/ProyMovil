package com.example.turnofacil.data.repository

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para la gestión de citas/turnos.
 */
interface AppointmentRepository {
    /** Observa todas las citas agendadas. */
    fun getCitas(): Flow<List<Cita>>

    /** Observa la agenda de citas para una fecha específica ("Hoy" o "yyyy-MM-dd"). */
    fun getCitasPorFecha(fecha: String): Flow<List<Cita>>

    /** Obtiene los detalles de una cita por su id de dominio. */
    suspend fun getCitaPorId(id: String): Cita?

    /** Observa los detalles de una cita de forma reactiva. */
    fun observarCitaPorId(id: String): Flow<Cita?>

    /** Agenda una nueva cita a partir de una solicitud enviada por la UI. */
    suspend fun agendarCita(solicitud: SolicitudAgendarCita): ResultadoAgendar

    /** Agenda una cita directamente (compatibilidad con objeto Cita). */
    suspend fun agregarCita(cita: Cita): ResultadoAgendar

    /** Confirma el estado de una cita. */
    suspend fun confirmarCita(id: String): Boolean

    /** Cancela una cita y libera su horario. */
    suspend fun cancelarCita(id: String): Boolean

    /** Cambia el estado de una cita. */
    suspend fun cambiarEstadoCita(id: String, nuevoEstado: EstadoCita): Boolean

    /** Reprograma fecha y hora de una cita existente. */
    suspend fun reprogramarCita(id: String, nuevaFecha: String, nuevaHora: String): ResultadoAgendar

    /** Verifica si un horario ya se encuentra ocupado por una cita activa. */
    suspend fun esHorarioOcupado(fecha: String, hora: String): Boolean

    /** Verifica si existen citas asociadas a un servicio específico. */
    fun tieneCitasAsociadasAServicio(servicioId: String): Boolean

    /** Observa el estado global de sincronización. */
    fun estaSincronizado(): Flow<Boolean>
}
