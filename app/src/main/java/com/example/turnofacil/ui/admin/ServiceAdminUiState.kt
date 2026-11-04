package com.example.turnofacil.ui.admin

import com.example.turnofacil.data.model.Servicio

/**
 * Estado inmutable de la pantalla Administración de Servicios (RF8).
 */
data class ServiceAdminUiState(
    val servicios: List<Servicio> = emptyList(),
    val isLoading: Boolean = false,
    val servicioEditando: Servicio? = null,
    val mostrarDialogoServicio: Boolean = false,
    val servicioParaBajaLogica: Servicio? = null,
    val mostrarDialogoBajaLogica: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
)
