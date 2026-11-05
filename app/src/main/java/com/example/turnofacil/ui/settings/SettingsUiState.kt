package com.example.turnofacil.ui.settings

/**
 * Estado inmutable de la pantalla Configuración (RF9 / RNF3).
 */
data class SettingsUiState(
    val idioma: String = "sistema",
    val horaApertura: String = "09:00",
    val horaCierre: String = "19:00",
    val syncAutomatica: Boolean = true,
    val ultimaSincronizacion: String = "Hoy, 09:30 a. m.",
    val mensajeExito: String? = null,
)
