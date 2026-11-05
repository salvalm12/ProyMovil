package com.example.turnofacil.data.repository

import com.example.turnofacil.data.model.TipoAtencion

// TODO(coordinar): mover a data/model cuando el dueño del dominio lo confirme

/**
 * Adicional disponible para complementar un servicio al agendar una cita.
 */
data class Adicional(
    val id: String = "",
    val nombre: String,
    val precio: Double,
    val activo: Boolean = true,
)

/**
 * Solicitud estructurada enviada por la UI para agendar un turno.
 */
data class SolicitudAgendarCita(
    val servicioId: String,
    val cliente: String,
    val tipoAtencion: TipoAtencion,
    val adicionales: List<String> = emptyList(),
    val hora: String,
    val fecha: String,
    val nota: String = "",
)

/**
 * Resultado de las operaciones de agendamiento y reprogramación.
 */
sealed interface ResultadoAgendar {
    data object Exito : ResultadoAgendar
    data object HorarioOcupado : ResultadoAgendar
    data class Error(val mensaje: String) : ResultadoAgendar

    fun onSuccess(action: () -> Unit): ResultadoAgendar {
        if (this is Exito) action()
        return this
    }

    fun onFailure(action: (Throwable) -> Unit): ResultadoAgendar {
        when (this) {
            is HorarioOcupado -> action(Exception("Horario ocupado"))
            is Error -> action(Exception(mensaje))
            else -> {}
        }
        return this
    }
}
