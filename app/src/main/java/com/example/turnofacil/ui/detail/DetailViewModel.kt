package com.example.turnofacil.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnofacil.data.container.DefaultAppContainer
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.repository.AppointmentRepository
import com.example.turnofacil.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Detalle de Cita (RF5).
 * Permite confirmar, reprogramar y cancelar citas.
 */

class DetailViewModel(
    private val citaId: String,
    private val appointmentRepository: AppointmentRepository = DefaultAppContainer().appointmentRepository,
    private val serviceRepository: ServiceRepository = DefaultAppContainer().serviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        cargarCita()
    }

    fun cargarCita() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val cita = appointmentRepository.getCitaPorId(citaId)
            if (cita != null) {
                val servicio = serviceRepository.getServicioPorId(cita.servicioId)
                val precioBase = servicio?.precio ?: 0.0
                // Regla de negocio 4: Total de la cita = precio servicio + adicionales
                val precioAdicionales = cita.adicionales.size * 50.0
                val precioTotal = precioBase + precioAdicionales

                _uiState.update {
                    it.copy(
                        cita = cita,
                        servicio = servicio,
                        precioBase = precioBase,
                        precioAdicionales = precioAdicionales,
                        precioTotal = precioTotal,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Cita no encontrada") }
            }
        }
    }

    fun confirmarCita() {
        viewModelScope.launch {
            val exito = appointmentRepository.cambiarEstadoCita(citaId, EstadoCita.CONFIRMADA)
            if (exito) {
                cargarCita()
                _uiState.update { it.copy(mensajeExito = "detail_exito_confirmada") }
            }
        }
    }

    fun cancelarCita() {
        viewModelScope.launch {
            // Regla de negocio 3: Cancelar cita libera su horario
            val exito = appointmentRepository.cambiarEstadoCita(citaId, EstadoCita.CANCELADA)
            if (exito) {
                cargarCita()
                _uiState.update { it.copy(mensajeExito = "detail_exito_cancelada") }
            }
        }
    }

    fun abrirDialogoReprogramar() {
        _uiState.update { it.copy(mostrarDialogoReprogramar = true) }
    }

    fun cerrarDialogoReprogramar() {
        _uiState.update { it.copy(mostrarDialogoReprogramar = false) }
    }


    fun reprogramarCita(nuevaFecha: String, nuevaHora: String) {
        viewModelScope.launch {
            val resultado = appointmentRepository.reprogramarCita(citaId, nuevaFecha, nuevaHora)
            resultado.onSuccess {
                cerrarDialogoReprogramar()
                cargarCita()
                _uiState.update { it.copy(mensajeExito = "detail_exito_reprogramada") }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }


    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensajeExito = null) }
    }
}
