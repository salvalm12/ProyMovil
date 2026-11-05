package com.example.turnofacil.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnofacil.data.container.DefaultAppContainer
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Agenda del día (RF4).
 * Maneja el filtro por fecha, filtro por estado (Pendiente / Confirmado),
 * la ordenación por hora y el estado de sincronización.
 */
class AgendaViewModel(
    private val appointmentRepository: AppointmentRepository = DefaultAppContainer().appointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        observarAgenda()
    }

    private fun observarAgenda() {
        viewModelScope.launch {
            combine(
                appointmentRepository.getCitas(),
                appointmentRepository.estaSincronizado()
            ) { todasLasCitas, sync ->
                val filtroEstado = _uiState.value.estadoFiltro
                val filtroFecha = _uiState.value.fechaFiltro

                val citasFiltradas = todasLasCitas
                    .filter { cita ->
                        val fechaCoincide = filtroFecha.equals("Hoy", ignoreCase = true) ||
                                cita.fecha.equals(filtroFecha, ignoreCase = true)
                        val estadoCoincide = filtroEstado == null || cita.estado == filtroEstado
                        fechaCoincide && estadoCoincide
                    }
                    .sortedBy { it.hora } // Ordenada por hora según RF4

                Pair(citasFiltradas, sync)
            }.collect { (citas, sync) ->
                _uiState.update { current ->
                    current.copy(
                        citas = citas,
                        estaSincronizado = sync,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun filtrarPorEstado(estado: EstadoCita?) {
        _uiState.update { it.copy(estadoFiltro = estado) }
    }

    fun seleccionarFecha(fecha: String) {
        _uiState.update { it.copy(fechaFiltro = fecha) }
    }

    fun confirmarCita(citaId: String) {
        viewModelScope.launch {
            appointmentRepository.cambiarEstadoCita(citaId, EstadoCita.CONFIRMADA)
        }
    }

    fun cancelarCita(citaId: String) {
        viewModelScope.launch {
            // Regla de negocio 3: Cancelar cita libera su horario
            appointmentRepository.cambiarEstadoCita(citaId, EstadoCita.CANCELADA)
        }
    }
}
