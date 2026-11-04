package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.model.TipoAtencion

data class AgendarCitaUiState(
    val serviciosDisponibles: List<Servicio> = emptyList(),
    val adicionalesDisponibles: List<Adicional> = emptyList(),
    val servicioSeleccionado: Servicio? = null,
    val nombreCliente: String = "",
    val tipoAtencion: TipoAtencion = TipoAtencion.LOCAL,
    val adicionalesSeleccionados: Set<Adicional> = emptySet(),
    val hora: String = "",
    val fecha: String = "Hoy",
    val horaApertura: String = "09:00", // TODO(temporal): obtener de Configuración / PreferenciasApp
    val horaCierre: String = "18:00",   // TODO(temporal): obtener de Configuración / PreferenciasApp
    val errorNombreVacio: Boolean = false,
    val errorHoraFueraHorario: Boolean = false,
    val errorHorarioOcupado: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val errorGeneral: String? = null,
) {
    val total: Double
        get() {
            val precioServicio = servicioSeleccionado?.precio ?: 0.0
            val precioAdicionales = adicionalesSeleccionados.sumOf { it.precio }
            return precioServicio + precioAdicionales
        }
}
