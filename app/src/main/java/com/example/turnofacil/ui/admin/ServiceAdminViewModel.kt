package com.example.turnofacil.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnofacil.data.container.DefaultAppContainer
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.repository.AppointmentRepository
import com.example.turnofacil.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Administración de Servicios (RF8).
 * Maneja el alta, edición y baja lógica (Regla de Negocio 6) de los servicios del negocio.
 */
class ServiceAdminViewModel(
    private val serviceRepository: ServiceRepository = DefaultAppContainer().serviceRepository,
    private val appointmentRepository: AppointmentRepository = DefaultAppContainer().appointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceAdminUiState())
    val uiState: StateFlow<ServiceAdminUiState> = _uiState.asStateFlow()

    init {
        cargarServicios()
    }

    private fun cargarServicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            serviceRepository.getServicios().collect { lista ->
                _uiState.update { it.copy(servicios = lista, isLoading = false) }
            }
        }
    }

    fun abrirDialogoCrear() {
        _uiState.update { it.copy(servicioEditando = null, mostrarDialogoServicio = true) }
    }

    fun abrirDialogoEditar(servicio: Servicio) {
        _uiState.update { it.copy(servicioEditando = servicio, mostrarDialogoServicio = true) }
    }

    fun cerrarDialogoServicio() {
        _uiState.update { it.copy(mostrarDialogoServicio = false, servicioEditando = null) }
    }

    fun guardarServicio(servicio: Servicio) {
        viewModelScope.launch {
            if (servicio.nombre.isBlank()) {
                _uiState.update { it.copy(mensajeError = "admin_error_nombre_vacio") }
                return@launch
            }
            if (servicio.precio <= 0) {
                _uiState.update { it.copy(mensajeError = "admin_error_precio_invalido") }
                return@launch
            }
            if (servicio.duracionMin <= 0) {
                _uiState.update { it.copy(mensajeError = "admin_error_duracion_invalida") }
                return@launch
            }

            val existe = _uiState.value.servicios.any { it.id == servicio.id }
            if (existe) {
                serviceRepository.actualizarServicio(servicio)
            } else {
                serviceRepository.agregarServicio(servicio)
            }
            cerrarDialogoServicio()
        }
    }

    fun solicitarEliminar(servicio: Servicio) {
        // Regla de negocio 6: Eliminar un servicio con citas asociadas lo marca como inactivo (baja lógica)
        val tieneCitas = appointmentRepository.tieneCitasAsociadasAServicio(servicio.id)
        if (tieneCitas) {
            _uiState.update {
                it.copy(
                    servicioParaBajaLogica = servicio,
                    mostrarDialogoBajaLogica = true
                )
            }
        } else {
            viewModelScope.launch {
                serviceRepository.eliminarServicio(servicio.id)
            }
        }
    }

    fun confirmarBajaLogica() {
        val servicio = _uiState.value.servicioParaBajaLogica
        if (servicio != null) {
            viewModelScope.launch {
                serviceRepository.eliminarServicio(servicio.id)
                cerrarDialogoBajaLogica()
            }
        }
    }

    fun cerrarDialogoBajaLogica() {
        _uiState.update {
            it.copy(
                mostrarDialogoBajaLogica = false,
                servicioParaBajaLogica = null
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeError = null, mensajeExito = null) }
    }
}
