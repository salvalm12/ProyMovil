package com.example.turnofacil.ui.detail

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio

/**
 * Estado inmutable de la pantalla Detalle de Cita (RF5).
 * Calcula el total según la Regla de Negocio 4 (precio servicio + precio adicionales).
 */
data class DetailUiState(
    val cita: Cita? = null,
    val servicio: Servicio? = null,
    val precioBase: Double = 0.0,
    val precioAdicionales: Double = 0.0,
    val precioTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val mostrarDialogoReprogramar: Boolean = false,
)
