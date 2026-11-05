package com.example.turnofacil.domain.model

import androidx.annotation.StringRes
import com.example.turnofacil.R

/** Tipo de atención que puede elegir el cliente al agendar una cita. */
enum class TipoAtencion(@param:StringRes val etiqueta: Int) {
    LOCAL(R.string.agendar_tipo_local),
    DOMICILIO(R.string.agendar_tipo_domicilio),
}

/** Estado en el que puede encontrarse una cita dentro de la agenda. */
enum class EstadoCita(@param:StringRes val etiqueta: Int) {
    PENDIENTE(R.string.estado_pendiente),
    CONFIRMADA(R.string.estado_confirmada),
    CANCELADA(R.string.estado_cancelada),
}

/**
 * Estado de sincronización de una fila local.
 *
 * Toda fila nueva nace [PENDIENTE]; el repositorio la marcará como
 * [SINCRONIZADO] cuando la API confirme el cambio, o como [ERROR] si falla.
 */
enum class SyncState {
    PENDIENTE,
    SINCRONIZADO,
    ERROR,
}

/** Tipo de notificación asociada a una cita (RF10). */
enum class TipoNotificacion {
    RECORDATORIO,
    CONFIRMACION,
    CANCELACION,
}

/** Dueño al que pertenece un archivo multimedia (RF11). */
enum class TipoPropietarioMedia {
    SERVICIO,
    CITA,
}

/** Servicio ofrecido por el negocio, con su precio y duración estimada. */
data class Servicio(
    val id: String,
    val nombre: String,
    val precio: Double,
    val duracionMin: Int,
    val activo: Boolean = true,
)

/** Cita o turno agendado por un cliente. */
data class Cita(
    val id: String,
    val cliente: String,
    val servicioId: String,
    val tipoAtencion: TipoAtencion,
    val adicionales: List<String> = emptyList(),
    val hora: String,
    val fecha: String,
    val estado: EstadoCita = EstadoCita.PENDIENTE,
    val nota: String = "",
)
