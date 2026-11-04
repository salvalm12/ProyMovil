package com.example.turnofacil.ui.agenda

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita

/**
 * Estado inmutable de la pantalla Agenda del día (RF4).
 */
data class AgendaUiState(
    val citas: List<Cita> = emptyList(),
    val fechaFiltro: String = "Hoy",
    val estadoFiltro: EstadoCita? = null,
    val estaSincronizado: Boolean = true,
    val isLoading: Boolean = false,
    val mensajeError: String? = null,
)
